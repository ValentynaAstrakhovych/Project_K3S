pipeline {
    options {
        ansiColor('xterm')
    }
    agent any
    tools {
        terraform 'tf1.6' // please make sure you have this tool installed in Jenkins and use proper version
    }
    stages {
        stage('Sparse Checkout') {
            steps {
                script {
                    checkout([$class: 'GitSCM', 
                              branches: [[name: 'Project_K3S']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions: [[
                                  $class: 'SparseCheckoutPaths', 
                                  sparseCheckoutPaths: [[path: 'Project_K3S/']]
                              ]],
                              userRemoteConfigs: [[
                                    url: 'git@github.com:LocalCoding/DevOps_May_24.git',
                                    credentialsId: 'jenkins_access_to_git' // please make sure you have this credential in Jenkins please use your credentials
                              ]]
                    ])
                }
            }
        }
        stage('Install JQ, kubectl and Ansible') {
            steps {
                sh '''
                curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | sudo gpg --yes --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
                echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list
                sudo apt-get update
                sudo apt-get install -y kubeadm=1.28.1-1.1 kubelet=1.28.1-1.1 kubectl=1.28.1-1.1
                sudo apt-get install -y ansible
                '''
            }
        }
        stage('Terraform Create - Main VPC') {
            steps {
                sh '''
                cd ./Project_K3S/cluster_init/terraform/main_vpc_config
                terraform init -input=false
                terraform plan -out=terraform.tfplan
                terraform apply -input=false terraform.tfplan
                '''
            }
        }

        stage('Terraform Create - Master Node') {
            steps {
                sh '''
                cd ./Project_K3S/cluster_init/terraform/master_node_config
                terraform init -input=false
                terraform plan -out=terraform.tfplan
                terraform apply -input=false terraform.tfplan
                
                # Save the master node private IP to a file

                terraform output -json k3s_master_instance_private_ip | jq -r 'if type == "array" then .[] else . end' > ../../ansible/master_ip.txt
                terraform output -json k3s_master_instance_public_ip | jq -r 'if type == "array" then .[] else . end' > ../../ansible/master_ip_public.txt
                
                k3s_master_instance_public_dns=$(terraform output -raw k3s_master_instance_public_dns)
                # Set it as an environment variable
                export k3s_master_instance_public_dns="$k3s_master_instance_public_dns"
                # Optional: Print the environment variable
                echo "The instance public DNS is set to: $k3s_master_instance_public_dns"

                '''
            }
        }
        stage('Terraform Create - Worker Node') {
            steps {
                sh '''
                cd ./Project_K3S/cluster_init/terraform/worker_node_config
                terraform init -input=false
                terraform plan -out=terraform.tfplan
                terraform apply -input=false terraform.tfplan
                
                sleep 30
 
                terraform apply -input=false terraform.tfplan
                terraform output -json k3s_workers_instance_private_ip | jq -r '.[]' > ../../ansible/worker_ip.txt
                '''
            }
        }
        stage('Run Ansible Playbooks') {
            steps {
                withCredentials([sshUserPrivateKey(credentialsId: 'access_for_new_node_js_app', keyFileVariable: 'SSH_KEY')]) {
                sh '''
                cd ./Project_K3S/cluster_init/ansible/
                ansible-playbook -i master_ip.txt master_setup.yml -u ubuntu --private-key=$SSH_KEY -e 'ansible_ssh_common_args="-o StrictHostKeyChecking=no"'
                ansible-playbook -i worker_ip.txt worker_setup.yml -u ubuntu --private-key=$SSH_KEY -e 'ansible_ssh_common_args="-o StrictHostKeyChecking=no"'
                '''
                }
            }
        }
        stage('Trigger Pacman Deployment') {
            steps {
                script {
                      // Trigger another Jenkins pipeline and wait for it to complete
                    def job = build job: 'deploy_pacman_k3s_cluster',
                                   wait: true
                    // Check if the triggered job failed and if so, fail the current job
                    if (job.result == 'FAILURE') {
                        error("Triggered job failed. Failing this job.")
                    }
                }
            }
        }

        // stage('Trigger Cluster Setup'){
        //     steps{
        //         script {
        //             // Trigger another Jenkins pipeline
        //             build job: 'cluster_setup',
        //                  wait: true
        //             // Check if the triggered job failed and if so, fail the current job
        //             if (job.result == 'FAILURE') {
        //                 error("Triggered job failed. Failing this job.")
        //             }
        //         }
        //     }
        // }

    }
}