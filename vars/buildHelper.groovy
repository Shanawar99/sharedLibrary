def buildMaven() {
    sh './mvnw -Dmaven.test.failure.ignore=true clean package'
}
def buildDockerImage(Map config) {
    sh "docker build -t ${config.RELEASE_NAME}:${config.IMAGE_TAG} ."
}
