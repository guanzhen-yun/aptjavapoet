package com.inke.compiler;

import com.google.auto.service.AutoService;
import com.inke.annotation.BindView;
import com.inke.annotation.HelloWorld;
import com.inke.compiler.utils.Constants;
import com.inke.compiler.utils.EmptyUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import jdk.nashorn.internal.runtime.ConsString;

//用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService({Processor.class})
//允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({Constants.BINDVIEW_ANNOTATION_TYPES})
//指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ButterKnifeProcessor extends AbstractProcessor {

    //操作Element工具类(类、函数、属性都是Element)
    private Elements elementUtils;

    //type（类信息）工具类，包含用于操作TypeMirror的工具方法
    private Types typeUtils;

    // Messager用来报告错误,警告和其他提示信息
    private Messager messager;

    //文件生成器 类/资源，Filer用来创建新的类文件，class文件以及辅助文件
    private Filer filer;

    //key:类节点,value:被@BindView注解的属性集合
    private Map<TypeElement, List<Element>> tempBindViewMap = new HashMap<>();

    //该方法主要用于一些初始化的操作，通过该方法的参数ProcessingEnviroment可以获取一些列有用的工具类
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //初始化
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        messager.printMessage(Diagnostic.Kind.NOTE, "注解处理器初始化完成，开始处理注解---------------------------->");
    }

    /**
     * 相当于main函数，开始处理注解
     * 注解处理器的核心方法，处理具体的注解，生成Java文件
     *
     * @param set 使用了支持处理注解多人节点集合
     * @param roundEnvironment 当前或是之前的运行环境，可以通过该对象查找的注解
     * @return true 表示后续处理器不会再处理（已经处理完成）
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 一旦有类之上使用@BindView注解
        if(!EmptyUtils.isEmpty(set)) {
            //获取所有被@BindView注解的 元素集合
            Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);

            if(!EmptyUtils.isEmpty(bindViewElements)) {
                try {
                    // 赋值临时map存储，用来存放被注解的属性集合
                    valueOfMap(bindViewElements);
                    //生成类文件，如:
                    createJavaFile();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void createJavaFile() throws IOException {

        //判断是否有需要生成的类文件
        if(EmptyUtils.isEmpty(tempBindViewMap)) return;

        //获取ViewBinder接口类型（生成类文件需要实现的接口）
        TypeElement viewBinderType = elementUtils.getTypeElement(Constants.VIEWBINDER);

        for (Map.Entry<TypeElement, List<Element>> entry : tempBindViewMap.entrySet()) {
            //类名
            ClassName className = ClassName.get(entry.getKey());
            //实现接口泛型
            ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get(viewBinderType),
                    ClassName.get(entry.getKey()));
            //参数体配置（MainActivity target）
            ParameterSpec parameterSpec = ParameterSpec.builder(ClassName.get(entry.getKey()),//MainActivity
                  Constants.TARGET_PARAMETER_NAME) //target
                  .build();

            //方法配置: public void bind(MainActivity target) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.BIND_METHOD_NAME) //方法名
                      .addAnnotation(Override.class) //重写注释
                      .addModifiers(Modifier.PUBLIC) //public修饰符
                      .addParameter(parameterSpec); //方法参数

            for (Element fieldElement : entry.getValue()) {
                //获取属性名
                String fieldName = fieldElement.getSimpleName().toString();
                //获取@BindView注解的值
                int annotationValue = fieldElement.getAnnotation(BindView.class).value();
                //target.tv = target.findViewById(R.id.tv);
                String methodContent = "$N." + fieldName + " = $N.findViewById($L)";
                methodBuilder.addStatement(methodContent,
                        Constants.TARGET_PARAMETER_NAME,
                        Constants.TARGET_PARAMETER_NAME,
                        annotationValue);
            }

            //必须是同包(属性修饰符缺省)，MainActivity$$ViewBinder
            JavaFile.builder(className.packageName(),//包名
                    TypeSpec.classBuilder(className.simpleName() + "$$ViewBinder") //类名
                        .addSuperinterface(typeName) //实现ViewBinder接口
                        .addModifiers(Modifier.PUBLIC) //public修饰符
                        .addMethod(methodBuilder.build()) //方法的构建（方法参数 + 方法体）
                        .build()) //类构建完成
                    .build() //JavaFile构建完成
                    .writeTo(filer);//文件生成器开始生成类文件
        }


    }

    private void valueOfMap(Set<? extends Element> bindViewElements) {
        if(!EmptyUtils.isEmpty(bindViewElements)) {
            for (Element element : bindViewElements) {
                //注解在属性之上，属性节点父节点是类节点
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                //如果map集合中的key: 类节点存在，直接添加属性
                if(tempBindViewMap.containsKey(enclosingElement)) {
                    tempBindViewMap.get(enclosingElement).add(element);
                } else {
                    List<Element> fields = new ArrayList<>();
                    fields.add(element);
                    tempBindViewMap.put(enclosingElement, fields);
                }
            }
        }
    }
}
