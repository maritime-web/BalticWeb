pipeline {
    agent any

    tools {
        maven 'M3.3.9'
    }

    triggers {
        pollSCM('H/5 * * * *')
        upstream(upstreamProjects: 'Enav-Services', threshold: hudson.model.Result.SUCCESS)
    }

    stages {
        stage('checkout') {
            steps {
                checkout scm
            }
        }

        stage('build') {
            steps {
                withMaven() {
                    sh 'mvn -e -U -DincludeSrcJavadocs clean source:jar compile checkstyle:check jslint4java:lint install'
                }
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

