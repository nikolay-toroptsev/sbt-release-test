pipeline {
    agent {
        label 'agent-with-docker'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        ansiColor('xterm2')
    }

    environment {
        KRB5_PASS = credentials('ml-engine-krb5pass')
        CONSUL_TOKEN = credentials('geo-analytics-test-consul-token')
        AGENT_HOSTNAME = sh(returnStdout: true, script: "hostname -I | cut -d' ' -f1").trim()
        ARTIFACTORY_USERNAME = "uploader"
        SBT_OPTS = "-Xmx3G"
    }

    parameters {
        booleanParam(name: 'RUN_ALL_STAGES_ON_MASTER', defaultValue: true, description: 'Uncheck to enable stages configuration on master')
        booleanParam(name: 'RELEASE', defaultValue: false, description: 'Release version')
        booleanParam(name: 'PACKAGE', defaultValue: true, description: 'Package jars')
        booleanParam(name: 'RUN_TEST', defaultValue: true, description: 'Run unit and integration tests')
        booleanParam(name: 'PUBLISH', defaultValue: true, description: 'Publish jars')
        booleanParam(name: 'DEPLOY_RELEASE', defaultValue: false, description: 'Deploy images to prod environment')
    }

    // def masterCheck = env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER

    stages {

        stage('Prepare env') {
            steps {
                sh "printenv"

            }
        }

        stage('Checkout') {
            steps {
                checkout scm
                sh 'git config user.name "Jenkins"'
                sh 'git config user.email "jenkins@onefactor.com"'
                sh 'sbt clean'
            }
        }

        stage('Release') {
            when {
                expression { return params.RELEASE && env.BRANCH_NAME == 'master' }
            }
            steps {
                sh 'sbt "release with-defaults default-tag-exists-answer o"'
            }
        }

        stage('Package') {
            when {
                expression { return (params.PACKAGE || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER) && !params.RELEASE) }
            }
            steps {
                sh 'sbt package'
            }
        }

        stage('Test') {
            when {
                expression { return (params.RUN_TEST || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER) && !params.RELEASE) }
            }
            steps {
                sh 'sbt test'

            }
        }

        stage('Publish') {
            when {
                expression { return params.PUBLISH || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER) }
            }
            steps {
                sh 'sbt publish'
            }
        }


        stage('Post release') {
            when {
                expression { return params.RELEASE && env.BRANCH_NAME == 'master' }
            }
            steps {
                sh 'git pull'
                sh 'sbt postRelease'
            }
        }

        stage('Deploy release') {
            when {
                expression { return params.DEPLOY_RELEASE && env.BRANCH_NAME == 'master' }
            }
            environment {
                RANCHER_SECRET_KEY = credentials('ml-engine_secret_key_prod_rancher')
                RANCHER_ENV = 'prod'
                RANCHER_STACK_NAME = 'sm-ml-engine'
            }
            steps {
                sh 'sbt deploy'
            }
        }
    }
}

//def getDefaultValue = env.BRANCH_NAME == 'master' ? true : false
