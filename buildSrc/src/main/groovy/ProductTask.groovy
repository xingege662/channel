import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ProductTask extends DefaultTask {

    def manifestFile

    def channelName

    ProductTask() {
        group '渠道号插件'
        description '将渠道号插入manifest中的meta-data中'
    }

    @TaskAction
    def run() {
        project.logger.quiet("操作manifest 增加 meta-data channel.value = ${channelName}")

        XmlParser xmlParser = new XmlParser()
        def xml = xmlParser.parse(manifestFile)

        def nameSpace = new groovy.xml.Namespace("http://schemas.android.com/apk/res/android", 'android')

        Node application = xml.application[0]

        def metaDataTags = application["meta-data"]

        metaDataTags.findAll {
            groovy.util.Node node ->
                node.attributes()[ns.name] == "channel"
        }.each {
            groovy.util.Node node ->
                node.parent().remove(node)
        }
        //将channel信息写入manifest文件中
        application.appendNode('meta-data', [(nameSpace.name): "channel", (nameSpace.value): channelName])
        def pw = new XmlNodePrinter(new PrintWriter(manifestFile, 'UTF-8'))
        pw.print(xml)
    }
}
