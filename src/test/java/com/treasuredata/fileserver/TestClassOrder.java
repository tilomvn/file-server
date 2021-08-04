package com.treasuredata.fileserver;

import com.treasuredata.fileserver.order.TestOrder;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

public class TestClassOrder implements ClassOrderer {

    @Override
    public void orderClasses(ClassOrdererContext classOrdererContext) {
        classOrdererContext.getClassDescriptors().sort(Comparator.comparingInt(TestClassOrder::getOrder));
    }

    private static int getOrder(ClassDescriptor classDescriptor) {
        if (classDescriptor.findAnnotation(TestOrder.class).isPresent()) {
            TestOrder testOrder = classDescriptor.findAnnotation(TestOrder.class).get();
            return testOrder.value();
        }
        return 0;
    }
}
