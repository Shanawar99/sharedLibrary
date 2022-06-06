def buildMaven()
{
    sh "./mvnw -Dmaven.test.failure.ignore=true clean package"
}
def buildDockerImage(Map config) {
    sh """docker build -t ${config.REPOSITORY_URI}:${config.IMAGE_TAG} ."""
}
