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
        stage('update pacman url') {
            steps {
                script {
                    sh '''
                    cd ./Project_K3S/cluster_entities/pacman
                    ./update_host_pacman.sh
                    cat packman-deployment.yaml
                    '''
                }
            }
        }
        stage('Install ingress and pacman in k3s') {
            steps {
                script {
                    sh '''
                    cd ./Project_K3S/cluster_init/aws_ingress_setup
                    kubectx default
                    kubectl get nodes
                    kubectl apply -f 2.nginx-ingress.yaml
                    sleep 30  
                    cd ../../cluster_entities/pacman
                    kubectl apply -f mongo-deployment.yaml -f packman-deployment.yaml
                    kubectl get pods -A
                    kubectl get ingresses -A
                    '''
                }
            }
        }
    }
}
