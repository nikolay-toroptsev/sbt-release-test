pipeline {
    agent {
        docker {
            image 'onef-docker-registry.jfrog.io/ml-build-env:0.1.0'
            label 'geomind'
            args "-u root " +
                    "--entrypoint=/entrypoint.sh " +
                    "-v ivy-home-${"${env.JOB_NAME}".replaceAll("\\/", "--")}:/root/.ivy2 " +
                    "-v global-sbt-boot:/root/.sbt/boot " +
                    "-v global-sbt-coursier-cache:/root/.coursier/cache " +
                    "-v global-pip-cache:/root/.cache/pip " +
                    "-v global-pipenv-cache:/root/.cache/pipenv " +
                    "-v /var/run/docker.sock:/var/run/docker.sock"
        }
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
        booleanParam(name: 'PACKAGE', defaultValue: true, description: 'Package jars')
        booleanParam(name: 'RUN_TEST', defaultValue: true, description: 'Run unit and integration tests')
        booleanParam(name: 'PUBLISH', defaultValue: true, description: 'Publish jars')
        booleanParam(name: 'BATCH_SERVICE_IMAGE', defaultValue: false, description: 'Build ml-engine-batch-service image')
        booleanParam(name: 'REPOSITORY_SERVICE_IMAGE', defaultValue: false, description: 'Build ml-engine-repository-service image')
        booleanParam(name: 'ONLINE_SERVICE_IMAGE', defaultValue: false, description: 'Build ml-engine-online-service image')
        booleanParam(name: 'JUPYTER_KERNEL', defaultValue: false, description: 'Build a jupyter kernel')
        booleanParam(name: 'RUN_PYTHON_TEST', defaultValue: false, description: 'Run python tests')
        booleanParam(name: 'RUN_SMOKE_TEST', defaultValue: false, description: 'Run ml-batch smoke tests')
        booleanParam(name: 'RUN_JUPYTER_SMOKE_TEST', defaultValue: false, description: 'Run jupyter smoke tests')
        booleanParam(name: 'DEPLOY_RELEASE', defaultValue: false, description: 'Deploy images to prod environment')
    }

    stages {

        stage('Prepare env') {
            steps {
                sh "printenv"
                script {
                    def secrets = [
                            [$class: 'VaultSecret', path: 'secret/onefartifactory/users/uploader', secretValues: [
                                   [$class: 'VaultSecretValue', envVar: 'ARTIFACTORY_PASSWORD', vaultKey: 'value']
                            ]]
                    ]

                    def configuration = [
                            $class: 'VaultConfiguration',
                            vaultUrl: 'https://consul-vault003-prod.infr.sel-vpc.onefactor.com:8200',
                            vaultCredentialId: 'approle-artifactory-uploader'
                    ]

                    wrap([$class: 'VaultBuildWrapper', configuration: configuration, vaultSecrets: secrets]) {
                        sh "docker volume create ivy-home-${"${env.JOB_NAME}".replaceAll("\\/", "--")}"
                        sh "docker volume create global-sbt-boot"
                        sh "docker volume create global-sbt-coursier-cache"
                        sh "docker volume create global-pip-cache"
                        sh "docker volume create global-pipenv-cache"
                        sh '''/entrypoint.sh'''
                    }
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
                sh 'sbt clean'
            }
        }

        stage('Package') {
            when {
                expression { return params.PACKAGE || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER) }
            }
            steps {
                sh 'sbt package'
            }
        }

        stage('Test') {
            when {
                expression { return params.RUN_TEST || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER) }
            }
            steps {
                parallel(
                    unitTest: {
                        sh 'sbt test'
                    },
                    integrationTest: {
                        sh 'sbt batch-jobs/assembly it:test'
                    }
                )
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

        stage('batch-service image') {
            when {
                expression { return params.BATCH_SERVICE_IMAGE || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER) }
            }
            steps {
                sh 'sbt batch-service/assembly batch-jobs/assembly batch-service/dockerBuildAndPush'
            }
        }

        stage('repository-service image') {
            when {
                expression { return params.REPOSITORY_SERVICE_IMAGE || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER) }
            }
            steps {
                sh 'sbt repository-service/assembly repository-service/dockerBuildAndPush'
            }
        }

        stage('online-service image') {
            when {
                expression { return params.ONLINE_SERVICE_IMAGE || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER) }
            }
            steps {
                sh 'sbt online-service/assembly online-service/dockerBuildAndPush'
            }
        }

        stage('jupyter kernel') {
            when {
                expression { return params.JUPYTER_KERNEL || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER) }
            }

            steps {
                script {
                    if (params.RUN_PYTHON_TEST || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER)) {
                        sh 'sbt testAndPublishPythonSequentially'
                    } else {
                        sh 'sbt publishPythonSequentially'
                    }
                }

                sh 'sbt jupyter/dockerBuildAndPush'
            }
        }

        stage('Smoke test') {
            when {
                expression { return params.RUN_SMOKE_TEST || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER) }
            }
            environment {
                RANCHER_SECRET_KEY = credentials('ml-engine_secret_key_test_rancher')
                RANCHER_ENV = 'test'
                PLATFORM = 'uat'
            }
            steps {
                sh 'sbt deploy'
                sh 'sleep 60'
                sh 'sbt smoke:test'
                sh 'sbt undeploy'
            }
        }

        stage('Jupyter smoke test') {
            when {
                expression { return params.RUN_JUPYTER_SMOKE_TEST || (env.BRANCH_NAME == 'master' && params.RUN_ALL_STAGES_ON_MASTER) }
            }
            steps {
                sh 'sbt jupyter/smoke:test'
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
