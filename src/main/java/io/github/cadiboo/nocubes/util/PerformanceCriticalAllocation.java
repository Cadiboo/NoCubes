package io.github.cadiboo.nocubes.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks the class as needing to have few allocations since it is used a lot.
 * Any classes marked with this must be pooled (i.e. use thread local instances).
 * Any classes marked with this are prime candidates to be converted to inline classes when
 * Project Valhalla makes its way into Java.
 * @author Cadiboo
 */
@Retention(RetentionPolicy.SOURCE)
public @interface PerformanceCriticalAllocation {
}
