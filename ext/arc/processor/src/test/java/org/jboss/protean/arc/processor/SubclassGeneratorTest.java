package org.jboss.protean.arc.processor;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.jboss.protean.arc.processor.Basics.index;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.protean.arc.processor.AnnotationLiteralProcessor;
import org.jboss.protean.arc.processor.BeanDeployment;
import org.jboss.protean.arc.processor.BeanGenerator;
import org.jboss.protean.arc.processor.BeanInfo;
import org.jboss.protean.arc.processor.BeanProcessor;
import org.jboss.protean.arc.processor.SubclassGenerator;
import org.jboss.protean.arc.processor.ResourceOutput.Resource;
import org.jboss.protean.arc.processor.types.Baz;
import org.junit.Test;

public class SubclassGeneratorTest {

    @Test
    public void testGenerator() throws IOException {

        Index index = index(SimpleBean.class, Simple.class, SimpleInterceptor.class, Baz.class);
        BeanDeployment deployment = new BeanDeployment(index, null);
        deployment.init();

        BeanGenerator beanGenerator = new BeanGenerator();
        SubclassGenerator generator = new SubclassGenerator();
        BeanInfo simpleBean = deployment.getBeans().stream()
                .filter(b -> b.getTarget().asClass().name().equals(DotName.createSimple(SimpleBean.class.getName()))).findAny().get();
        for (Resource resource : beanGenerator.generate(simpleBean, new AnnotationLiteralProcessor(BeanProcessor.DEFAULT_NAME, true), ReflectionRegistration.NOOP)) {
            generator.generate(simpleBean, resource.getFullyQualifiedName(), ReflectionRegistration.NOOP);
        }
        // TODO test generated bytecode
    }

    @Dependent
    static class SimpleBean {

        private Baz baz;

        @Inject
        public SimpleBean(Baz baz) {
            this.baz = baz;
        }

        @Simple
        String foo(String bar) {
            return "" + baz.isListResolvable();
        }

        Integer fooNotIntercepted() {
            return 1;
        }

    }

    @Simple
    @Priority(1)
    @Interceptor
    public class SimpleInterceptor {

        @AroundInvoke
        Object intercept(InvocationContext ctx) throws Exception {
            return ctx.proceed();
        }
    }

    @Target({ TYPE, METHOD })
    @Retention(RUNTIME)
    @Documented
    @InterceptorBinding
    public @interface Simple {

    }

}
