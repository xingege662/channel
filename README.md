# channel
#### ## 如何理解gradle？Gradle在Android的构建过程中起到了什么作用？
1.gradle是Android中项目的构建工具，通过gradle，我们很容易进行分模块编程，将大事化小，从而清晰的进行模块开发。从maven演变过来，比maven更灵活。

2.首先，在Android的构建过程中，有很多常用的配置，也可以叫方法，通过这些个配置，我们能很容易的通过gradle来指定项目中所用的版本号，包名，还有第三方库的依赖等等。

3.常用的配置如果下

- 项目gradle中的配置
```
//构建脚本的配置
buildscript {
    //仓库
    repositories {
        jcenter()
    }
    //依赖
    dependencies {
        classpath 'com.android.tools.build:gradle:2.3.1'
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

//配置子模块的共用行为
allprojects {
    repositories {
        jcenter()
    }
}
//clean 任务
task clean(type: Delete) {
    delete rootProject.buildDir
}
```
- 模块gradle中的配置

```
//引入插件
apply plugin: 'com.android.application'
//配置android插件
android {
    //编译使用的sdk版本
    compileSdkVersion 25
    // buildtools版本
    buildToolsVersion "25.0.3"
    //默认 产品风味
    defaultConfig {
        //包名 执行构建时会替换调manifest当中的 package节点
        //原package节点会拼接 给name节点以‘.’开头的 组件
        applicationId "com.cx.gradle"
        //最小支持的sdk版本
        minSdkVersion 14
        //target版本
        targetSdkVersion 25
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
        applicationIdSuffix '.default'
//        setManifestPlaceholders([:])
        //manifest占位符
        manifestPlaceholders = [name:'1']
        //分包 65k
        multiDexEnabled true

        //构建ndk的配置
//        externalNativeBuild {
//            //android.mk
//            ndkBuild {
//
//            }
//            //cmake
//            cmake{
//
//            }
//        }

        //支持lambda 部分特性
//        jackOptions{
//            enabled true
//        }


//        proguardFiles
        //定义打包的cpu架构支持
        ndk{
//            abiFilters 'armeabi-v7a'
        }

        //dimension
        // vectorDrawables
        //
        vectorDrawables{
            //开启支持后 不会再生成png
            //如果关闭 并且 minsdk 小于21 则 会根据generatedDensities生成png
            useSupportLibrary = true
            generatedDensities = ['mdpi']
        }
        //BuildConfig
        buildConfigField('String','h','"12adasdsa"')
        //res/value
        resValue('string','hh','asdad')
//        dimension  维度
    }

    //创建两个维度
    flavorDimensions('product','abi')
    //创建产品风味
    productFlavors {
        free{
            dimension 'product'
            manifestPlaceholders = [name:'2']
            applicationIdSuffix '.free'
        }

        pro{
            dimension 'product'
        }
        armeabiV7a{
            dimension 'abi'
        }
        x86{
            dimension 'abi'
        }
    }
    //过滤变体
    variantFilter {
        variant->
            variant.flavors.each{
                if (it.name.contains('pro')){
                    setIgnore(true)
                }
            }
    }

    //指定ndk的构建文件
//    externalNativeBuild{
//        ndkBuild {
//            path 'Android.mk'
//        }
//        cmake{
//            path 'CMakeLists.txt'
//        }
//    }
     signingConfigs {
        mySign {
            storeFile file('debug.keystore') //用file方法，表示这个文件在builde.gradle的这个目录下，是个相对目录，要根据自己的签名文件配路径
            storePassword 'android'
            keyAlias 'androiddebugkey'
            keyPassword 'android'
        }
    }
    //构建类型
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
     sourceSets {
        main {
            res.srcDirs 'src/main/res', 'src/main/myres'
            java.srcDir('src/main/java')
//            aidl.srcDirs ''
//            assets
            //compileConfigurationName //compile 编译+打包的依赖配置组的名字
//            packageConfigurationName //apk
//            providedConfigurationName //provided
//            java
//            jni
//            jniLibs
//            manifest.srcFile
//            name
//            renderscript
//            res
//            resources
        }

        hello{

        }
    }

}

configurations{
    freeArmeabiV7aDebugCompile{}
}

//依赖配置
dependencies {
    freeArmeabiV7aDebugCompile project(':lib:library')
    compile fileTree(dir: 'libs', include: ['*.jar'])
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    compile 'com.android.support.constraint:constraint-layout:1.0.2'
    testCompile 'junit:junit:4.12'
}

```
上面是一些常用的配置，还有一些不常用的，可以通过文档来查阅

#### 如何在打包过程中把渠道号插入apk，并在apk启动的时候拿到这个apk。

思路分析：想要hook manifest，需要找到打包操作中处理manifest的任务，然后我们自己写的gradle插件，依赖他，然后通过解析gradle插件，在打包完成之前，将meta-data插入到manifest中。

1.先声明插件，并且在分析完之后，拿到manifest文件给自定义的task，这个task用来解析manifest文件，并且写入meta-data渠道信息。

![image](http://opy4iwqsf.bkt.clouddn.com/WX20171223-225808@2x.png)

2.解析并写入渠道信息
![image](http://opy4iwqsf.bkt.clouddn.com/WX20171223-225958@2x.png)
3.拿到manifest文件的任务，自定义的任务要在它后面执行
![image](http://opy4iwqsf.bkt.clouddn.com/WX20171223-230058@2x.png)
4.主工程依赖插件
![image](http://opy4iwqsf.bkt.clouddn.com/WX20171223-225247@2x.png)
5.打包看效果
![image](http://opy4iwqsf.bkt.clouddn.com/WX20171223-230240@2x.png)

![image](http://opy4iwqsf.bkt.clouddn.com/WX20171223-230332@2x.png)

![image](http://opy4iwqsf.bkt.clouddn.com/WX20171223-230757@2x.png)

![image](http://opy4iwqsf.bkt.clouddn.com/WX20171223-230730@2x.png)


项目地址：https://github.com/xingege662/channel/tree/master
