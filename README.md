# PandoraDoc
doc generater and reader for Pandora

利用我所写的[Pandora](https://github.com/leobert-lan/Pandora)类库可方便快捷的在Android中进行碎片化界面开发，但是管理和文档维护是一个比较头疼的问题；结合我所写的[ReportPrinter](https://github.com/leobert-lan/ReportPrinter)类库可以基于注解定制自己的文档，对于文档的维护花费时间相对合理，但是当开发的碎片组件达到一定的量级之后，文档阅读也是一个比较麻烦的问题，一般的markdown书写、阅读工具在该场景下都不算太friendly。

本sample用于示例如何用ReportPrinter定制文档，并结合知名开源方案[Markwon](https://github.com/noties/Markwon)编写一个轻量级的Android版文档阅读器，方便查阅Pandora碎片组件的文档

* app module 用于手机上查看doc，
* vh_reporter module 基于JSR305和SPI生成文档，但是目前gradle高于5.0时存在问题
* sample module 展示了如何使用

注解处理的环境变量

```
javaCompileOptions {
            annotationProcessorOptions {
                arguments = [module: "sample",
                             mode:"mode_file",
                             active_reporter:"on"]
            }
        }
```

引用包含注解的库：

```
dependencies {
    implementation project(":vh_reporter")
}
```

为了避免每次编译都进行处理，我们可以将该部分内容独立，避免资源消耗，以及windows反复出现的文件占用问题：

```

//use command line:'gradle clean :sample:printReporter :sample:compileDebugSource'
task printReporter {
    doFirst {
        project.dependencies.add("annotationProcessor", 'osp.leobert.android:report-anno-compiler:1.1.1')
        project.dependencies.add("annotationProcessor", 'org.apache.commons:commons-lang3:3.4')
        project.dependencies.add("annotationProcessor", 'org.apache.commons:commons-collections4:4.1')

        println("add vh reporter ++++++++++")
        project.dependencies.add("annotationProcessor", project(':vh_reporter'))
    }
}
```

使用命令行,执行任务，compileDebugSources根据具体情况走，配置过flavor的会存在命令变种：

```
use command line:'gradle clean :sample:printReporter :sample:compileDebugSources'
```

生成文件路径：

{projectroot}/Reports/SampleViewHoldersReport.md

---
