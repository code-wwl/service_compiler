package com.humble.service_compiler;

import com.google.auto.service.AutoService;
import com.humble.annotation.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

/**
 * @author wwl
 * @date 2020/11/26
 * 扫描注解类
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"com.humble.annotation.Service"})
@AutoService(Processor.class)
public class ServiceProcessor extends AbstractProcessor {
    private Filer filer;
    List<ServiceClassGeneratorBuilder> builders = new ArrayList<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) {
            System.out.println("---------initClass---------");
            processInitClass();
        } else {
            System.out.println("---------createClass---------");
            Set<? extends TypeElement> routeElements = (Set<? extends TypeElement>) roundEnvironment.getElementsAnnotatedWith(Service.class);
            processAnnotation(routeElements);
        }
        return true;
    }

    private void processInitClass() {
        System.out.println("size::: " + builders.size());
        for (ServiceClassGeneratorBuilder builder : builders) {
            builder.build();
        }
    }

    private void processAnnotation(Set<? extends TypeElement> routeElements) {
        for (TypeElement element : routeElements) {
            Service service = element.getAnnotation(Service.class);
            ServiceClassGeneratorBuilder builder = ServiceClassGeneratorBuilder.create(filer);
            builder.serviceName(getServiceValue(service))
                    .implName(element.getQualifiedName().toString());
            builders.add(builder);
        }
    }

    public String getServiceValue(Service service) {
        try {
            return service.value().getName();
        } catch (MirroredTypeException exception) {
            return exception.getTypeMirror().toString();
        }
    }


}