import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class ProductFlavorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin('com.android.application')) {
            throw new GradleException("只能与android application同时使用")
        }
        project.afterEvaluate {
            //对变体进行遍历
            project.android.applicationVariants.all {
                com.android.build.gradle.internal.api.ApplicationVariantImpl variant ->
                    def taskName = "${variant.flavorName.capitalize()}${variant.buildType.name.capitalize()}"

                    //创建task，处理manifest，插入meta-data信息
                    ProductTask metaDataTask = project.tasks.create("Channel${taskName}", ProductTask)
                    //获取打包过程中的menifest文件信息 ，并在task中处理
                    metaDataTask.manifestFile = variant.outputs.first().processManifest.manifestOutputFile
                    //获取applicationId当做渠道号,打入manifes的meta-data中
                    metaDataTask.channelName = variant.getName()
                    //已经存在manifest文件并且在打包前
                    metaDataTask.mustRunAfter variant.outputs.first().processManifest
                    variant.outputs.first().processResources.dependsOn metaDataTask
            }
        }
    }
}