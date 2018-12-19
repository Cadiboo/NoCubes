package clickme.nocubes.gui;

import clickme.nocubes.NoCubes;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiNoCubes extends GuiScreen {
   public void func_73866_w_() {
      this.field_146292_n.clear();
      this.field_146292_n.add(new GuiButton(0, this.field_146294_l / 2 - 100, this.field_146295_m / 2 - 30, 200, 20, "No Cubes general: " + (NoCubes.isNoCubesEnabled ? "enabled" : "disabled")));
      GuiButton button = new GuiButton(1, this.field_146294_l / 2 - 100, this.field_146295_m / 2 + 20, 200, 20, "Auto step: " + (NoCubes.isAutoStepEnabled ? "enabled" : "disabled"));
      button.field_146124_l = false;
      this.field_146292_n.add(button);
   }

   protected void func_146284_a(GuiButton button) {
      if (button.field_146124_l) {
         if (button.field_146127_k == 0) {
            NoCubes.isNoCubesEnabled = !NoCubes.isNoCubesEnabled;
            button.field_146126_j = "No Cubes general: " + (NoCubes.isNoCubesEnabled ? "enabled" : "disabled");
         }

         if (button.field_146127_k == 1) {
            NoCubes.isAutoStepEnabled = !NoCubes.isAutoStepEnabled;
            button.field_146126_j = "Auto step: " + (NoCubes.isAutoStepEnabled ? "enabled" : "disabled");
         }
      }

   }

   public void func_73876_c() {
      super.func_73876_c();
   }

   public void func_146281_b() {
      NoCubes.saveConfig();
      this.field_146297_k.func_110436_a();
   }

   public void func_73863_a(int i, int j, float f) {
      this.func_146276_q_();
      this.func_73732_a(this.field_146289_q, "No Cubes Settings", this.field_146294_l / 2, this.field_146295_m / 2 - 60, 16777215);
      this.func_73732_a(this.field_146289_q, "(overrides all other options)", this.field_146294_l / 2, this.field_146295_m / 2 - 5, 16777215);
      super.func_73863_a(i, j, f);
   }
}
