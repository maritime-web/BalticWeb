pipeline {
    agent any

    tools {
        maven 'M3.3.9'
    }

    triggers {
        pollSCM('H/5 * * * *')
        upstream(upstreamProjects: 'Enav-Services/master', threshold: hudson.model.Result.SUCCESS)
    }

    stages {
        stage('build') {
            steps {
                withMaven(options: [junitPublisher(ignoreAttachments: false), artifactsPublisher()]) {
                    sh 'mvn -e -U -DincludeSrcJavadocs clean source:jar compile checkstyle:check jslint4java:lint install'
                }
            }
        }

        stage('Docker Build on DockerHub') {
            when {
                branch 'master'
            }
            steps {
                sh 'curl -H "Content-Type: application/json" --data "{"source_type": "Branch", "source_name": "master"}" -X POST https://registry.hub.docker.com/u/dmadk/balticweb/trigger/8c29776d-ddd7-4b19-8919-9df0366af7eb/'
            }
        }
    }


    post {
        failure {
            // notify users when the Pipeline fails
            mail to: 'steen@lundogbendsen.dk',
                    subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                    body: "Something is wrong with ${env.BUILD_URL}"
        }
    }
}

