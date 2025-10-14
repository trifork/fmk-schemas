libraries{
    maven
    docker {
        java_version = 11
    }
    sonarqube_maven
    archive {
        artifacts = 'fmk-schemas/mc160_wsdl/target/*.zip, fmk-schemas/mc160idws_wsdl/target/*.zip, fmk-schemas/mc160_wsdl/target/resources/wsdl/*.wsdl, fmk-schemas/mc160idws_wsdl/target/resources/wsdl/*.wsdl'
    }
}