package net.minecraft.block;

public static class SoundType
{
    public final String field_150501_a;
    public final float field_150499_b;
    public final float field_150500_c;
    private static final String __OBFID = "CL_00000203";
    
    public SoundType(final String p_i45393_1_, final float p_i45393_2_, final float p_i45393_3_) {
        this.field_150501_a = p_i45393_1_;
        this.field_150499_b = p_i45393_2_;
        this.field_150500_c = p_i45393_3_;
    }
    
    public float func_150497_c() {
        return this.field_150499_b;
    }
    
    public float func_150494_d() {
        return this.field_150500_c;
    }
    
    public String func_150495_a() {
        return "dig." + this.field_150501_a;
    }
    
    public String func_150498_e() {
        return "step." + this.field_150501_a;
    }
    
    public String func_150496_b() {
        return this.func_150495_a();
    }
}
