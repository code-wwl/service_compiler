package com.humble.service_compiler;

import com.humble.annotation.util.Constants;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * @author wenlong wang
 * @date 2020/12/2 10:18
 */
public class ServiceClassGeneratorBuilder {

    private static final String classHeadName = "ServiceLoader_";
    private Filer filer;
    private String serviceName;
    private String serviceImplName;


    private ServiceClassGeneratorBuilder(Filer filer) {
        this.filer = filer;

    }

    static ServiceClassGeneratorBuilder create(Filer filer) {
        return new ServiceClassGeneratorBuilder(filer);
    }

    public ServiceClassGeneratorBuilder serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ServiceClassGeneratorBuilder implName(String implName) {
        this.serviceImplName = implName;
        return this;
    }


    public void build() {
        TypeSpec typeSpec = createTypeSpec();
        JavaFile javaFile = JavaFile.builder(Constants.SERVICE_PKG_NAME, typeSpec)
                .build();
        try {
            System.out.println(filer);
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeSpec createTypeSpec() {
        return TypeSpec.classBuilder(classHeadName + hash(serviceImplName))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(createMethod())
                .build();
    }

    private MethodSpec createMethod() {
        ClassName routerStore = ClassName.get("com.humble.router.remote", "RouterStore");

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("init");
        methodBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        methodBuilder.addCode("$T instance = $N.getInstance();\n", routerStore, "RouterStore");
        methodBuilder.addStatement("$N.register($S,$S)", "instance", serviceName, serviceImplName);
        methodBuilder.returns(void.class);
        return methodBuilder.build();
    }

    public static String hash(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(str.hashCode());
        }
    }
}
