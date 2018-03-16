package org.generousg.fruitylib.util

import kotlin.reflect.KClass
import kotlin.reflect.full.cast


class AnnotationMap() {
    private val annotations = hashMapOf<KClass<out Annotation>, Annotation>()

    constructor(annotations: Array<Annotation>) : this() {
        for(annotation in annotations) this.annotations.put(annotation.annotationClass, annotation)
    }

    fun hasAnnotation(cls: KClass<out Annotation>) = annotations[cls] != null
    operator fun <T: Annotation> get(cls: KClass<out T>): T {
        val a = annotations[cls]
        return cls.cast(a)
    }
    fun put(a: Annotation) {
        annotations.put(a.annotationClass, a)
    }
}