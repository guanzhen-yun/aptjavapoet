package com.inke.compiler;

import com.google.auto.service.AutoService;
import com.google.common.io.MoreFiles;
import com.inke.annotation.ARouter;
import com.inke.annotation.HelloWorld;
import com.inke.compiler.utils.Constants;
import com.inke.compiler.utils.EmptyUtils;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

//用来生成 META-INF/services/javax.annotation.processing.Processor文件
@AutoService({Processor.class})
//允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({Constants.HELLOWORLD_ANNOTATION_TYPES})
//指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class HelloWorldProcessor extends AbstractProcessor {

    // Messager用来报告错误,警告和其他提示信息
    private Messager messager;

    //文件生成器 类/资源，Filer用来创建新的类文件，class文件以及辅助文件
    private Filer filer;

    //该方法主要用于一些初始化的操作，通过该方法的参数ProcessingEnviroment可以获取一些列有用的工具类
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //初始化
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        messager.printMessage(Diagnostic.Kind.NOTE, "注解处理器初始化完成，开始处理注解---------------------------->");
    }

    /**
     * 相当于main函数，开始处理注解
     * 注解处理器的核心方法，处理具体的注解，生成Java文件
     * @param set 使用了支持处理注解多人节点集合
     * @param roundEnvironment 当前或是之前的运行环境，可以通过该对象查找的注解
     * @return true 表示后续处理器不会再处理（已经处理完成）
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 一旦有类之上使用@HelloWorld注解
        if(!EmptyUtils.isEmpty(set)) {
            //获取所有被@HelloWorld注解的 元素集合
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(HelloWorld.class);

            if(!EmptyUtils.isEmpty(elements)) {
                try {
                    //解析元素，生成类文件
                    parseElements(elements);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void parseElements(Set<? extends Element> elements) throws IOException {
        //方法体
        MethodSpec main = MethodSpec.methodBuilder("main") //方法名
             .addModifiers(Modifier.PUBLIC, Modifier.STATIC)//方法修饰符
             .returns(void.class) //方法返回值 （默认void）
            .addParameter(String[].class, "args") //方法参数
            .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!") //方法内容
            .build();

        //类
        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld") //类名
               .addModifiers(Modifier.PUBLIC, Modifier.FINAL) //类修饰符
               .addMethod(main) //加入方法体
               .build();

        //文件生成器（生成在指定 = 包名 + 类）
        JavaFile javaFile = JavaFile.builder("com.inke.helloworld", helloWorld)
                .build();

        //写文件
        javaFile.writeTo(filer);

    }
}
