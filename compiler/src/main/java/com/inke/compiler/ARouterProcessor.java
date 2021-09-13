package com.inke.compiler;

import com.google.auto.service.AutoService;
import com.inke.annotation.ARouter;
import com.inke.compiler.utils.Constants;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

//用来生成 META-INF/services/javax.annotation.processing.Processor文件
@AutoService({Processor.class})
//允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({Constants.AROUTER_ANNOTATION_TYPES})
//指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ARouterProcessor extends AbstractProcessor {
    //操作Element工具类(类、函数、属性都是Element)
    private Elements elementUtils;

    // Messager用来报告错误,警告和其他提示信息
    private Messager messager;

    //文件生成器 类/资源，Filer用来创建新的类文件，class文件以及辅助文件
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //初始化
        elementUtils = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

//    @Override
//    public Set<String> getSupportedAnnotationTypes() {
//        return super.getSupportedAnnotationTypes();
//        //支持的注解类型
//    }
//
//    @Override
//    public SourceVersion getSupportedSourceVersion() {
//        return super.getSupportedSourceVersion();
//        //指定JDK的编译版本
//    }
//
//    @Override
//    public Set<String> getSupportedOptions() {
//        return super.getSupportedOptions();
//        //接收参数
//    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if(set.isEmpty()) return false;

        //获取所有带ARouter注解的 类节点
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
        //遍历所有类节点
        for (Element element : elements) {
            //通过类节点获取包节点（全路径：com.netease.xxx）
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            //获取简单类名
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被注解的类有: " + className);
            //最终想生成的类文件名
            String finalClassName = className + "$$ARouter";

            //公开课写法，也是EventBus写法(https://github.com/greenrobot/EventBus)
            try {
                //创建一个新的源文件（Class），并返回一个对下以允许写入它
                JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + finalClassName);
                //定义Writer对象,开启写入
                Writer writer = sourceFile.openWriter();
                //设置包名
                writer.write("package " + packageName + ";\n");

                writer.write("import android.util.Log;\n");

                writer.write("public class " + finalClassName + " {\n");

                writer.write("public void hello(String path) {\n");

                writer.write("Log.e(\"netease >>> \", \"APT~~~\");\n}");

                writer.write("\n}");

                //最后结束别忘了
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
