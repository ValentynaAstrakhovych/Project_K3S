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
        stage('Trigger Another Pipeline') {
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
    }
}