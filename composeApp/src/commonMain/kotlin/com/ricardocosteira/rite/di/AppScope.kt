package com.ricardocosteira.rite.di

import me.tatarka.inject.annotations.Scope

/**
 * Scope annotation for application-level singletons.
 * Dependencies marked with @AppScope will be created once and reused throughout the app lifecycle.
 */
@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class AppScope
