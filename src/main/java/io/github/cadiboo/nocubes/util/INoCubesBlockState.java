package io.github.cadiboo.nocubes.util;

/**
 * IBlockProperties and BlockStateContainer$StateImplementation are made to implement this interface with ASM at runtime.
 * See IBlockPropertiesTransformer and StateImplementationTransformer.
 */
public interface INoCubesBlockState {

    /** Does NOT take into account whether NoCubes is enabled or not. */
    boolean nocubes_isSmoothable();
    void nocubes_setSmoothable(boolean smoothable);

}
