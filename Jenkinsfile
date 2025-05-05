pipeline {
    agent any
    
    environment {
        // 환경 변수 설정
        DOCKER_IMAGE = 'almagest-auth-server'
        DOCKER_TAG = "latest"
        ECR_REGISTRY = '485298580046.dkr.ecr.ap-northeast-2.amazonaws.com/almagest-auth'  // ECR 레지스트리 주소
        AWS_REGION = 'ap-northeast-2'  // AWS 리전
        INSTANCE_ID = 'your-instance-id'  // EC2 인스턴스 ID
        DOCKER_NETWORK = 'almagest-network'  // Docker 네트워크 이름
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh './gradlew clean build -x test'
            }
        }
        
        stage('Test') {
            steps {
                sh './gradlew test'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }
        
        stage('Docker Push to ECR') {
            steps {
                script {
                    // AWS 자격 증명 설정
                    withAWS(region: AWS_REGION, credentials: 'aws-credentials') {
                        // ECR 로그인
                        sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}"
                        
                        // 이미지 태그 및 푸시
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                    }
                }
            }
        }
        
        stage('Deploy to Server') {
            steps {
                script {
                    // SSM을 통한 명령 실행
                    withAWS(region: AWS_REGION, credentials: 'aws-credentials') {
                        // Docker 네트워크 생성 (없는 경우)
                        sh """
                            aws ssm send-command \
                            --instance-ids ${INSTANCE_ID} \
                            --document-name "AWS-RunShellScript" \
                            --parameters 'commands=["docker network create ${DOCKER_NETWORK} || true"]'
                        """
                        
                        // ECR 로그인
                        sh """
                            aws ssm send-command \
                            --instance-ids ${INSTANCE_ID} \
                            --document-name "AWS-RunShellScript" \
                            --parameters 'commands=["aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}"]'
                        """
                        
                        // 기존 컨테이너 중지 및 제거
                        sh """
                            aws ssm send-command \
                            --instance-ids ${INSTANCE_ID} \
                            --document-name "AWS-RunShellScript" \
                            --parameters 'commands=["docker stop ${DOCKER_IMAGE} || true", "docker rm ${DOCKER_IMAGE} || true"]'
                        """
                        
                        // 새 이미지 가져오기
                        sh """
                            aws ssm send-command \
                            --instance-ids ${INSTANCE_ID} \
                            --document-name "AWS-RunShellScript" \
                            --parameters 'commands=["docker pull ${ECR_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}"]'
                        """
                        
                        // 새 컨테이너 실행
                        sh """
                            aws ssm send-command \
                            --instance-ids ${INSTANCE_ID} \
                            --document-name "AWS-RunShellScript" \
                            --parameters 'commands=["docker run -d --name ${DOCKER_IMAGE} --network ${DOCKER_NETWORK} -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod ${ECR_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}"]'
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            // 빌드 후 정리 작업
            cleanWs()
        }
        success {
            // 성공 시 알림
            echo 'Build and deployment completed successfully!'
        }
        failure {
            // 실패 시 알림
            echo 'Build or deployment failed!'
        }
    }
} 