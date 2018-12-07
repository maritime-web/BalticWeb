pipeline {
    agent any

    tools {
        maven 'apache-maven-3.3.9'
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

