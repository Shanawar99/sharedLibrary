def createNamespace(Map configNameSpace) {
    sh "kubectl get namespace | grep ${configNameSpace.NAMESPACE} || kubectl create namespace ${configNameSpace.NAMESPACE}"
}

def deployRelease(Map configRelease) {
    sh "cd ./kubernetes/helm/k8s && helm install -f ./values-green.yaml ${configRelease.RELEASE_NAME}-${configRelease.IMAGE_TAG} --set=image.repository=${configRelease.REPOSITORY_URI} --set=image.tag=${configRelease.IMAGE_TAG} --namespace ${configRelease.NAMESPACE} . "
    sh "kubectl get services --namespace ${configRelease.NAMESPACE} --field-selector metadata.name=${configRelease.RELEASE_NAME}-${configRelease.IMAGE_TAG}"
}

def promoteRelease(Map configRelease) {
    sh "cd ./kubernetes/helm/k8s && helm upgrade --install -f ./values-green.yaml -f ./values.yaml ${configRelease.RELEASE_NAME} --set=image.repository=${configRelease.REPOSITORY_URI} --set=image.tag=${configRelease.IMAGE_TAG} --namespace ${configRelease.NAMESPACE} . "
    sh "kubectl get services --namespace ${configRelease.NAMESPACE} --field-selector metadata.name=${configRelease.RELEASE_NAME}-${configRelease.IMAGE_TAG}"
}


def removeRelease(Map configRemoveRelease) {
    sh "helm uninstall ${configRemoveRelease.RELEASE_NAME}-${configRemoveRelease.IMAGE_TAG} --namespace ${configRemoveRelease.NAMESPACE}"
    sh "kubectl get services --namespace ${configRemoveRelease.NAMESPACE} --field-selector metadata.name=${configRemoveRelease.RELEASE_NAME}-${configRemoveRelease.IMAGE_TAG}"

}

def pushImageToECR(Map config)
{
    sh """aws ecr describe-repositories --repository-names ${config.ECR_REPO_NAME} || aws ecr create-repository --repository-name ${config.ECR_REPO_NAME}"""
    sh """aws ecr get-login-password --region ${config.AWS_REGION} | docker login --username AWS --password-stdin ${config.AWS_ACCOUNT_ID}.dkr.ecr.${config.AWS_REGION}.amazonaws.com"""
    sh """docker push ${config.AWS_ACCOUNT_ID}.dkr.ecr.${config.AWS_REGION}.amazonaws.com/${config.ECR_REPO_NAME}:${IMAGE_TAG} """

}

def updateKubeconfig(Map configKube)
{
    sh """aws eks update-kubeconfig --name ${configKube.CLUSTER_NAME} --region ${configKube.AWS_REGION} """

}
