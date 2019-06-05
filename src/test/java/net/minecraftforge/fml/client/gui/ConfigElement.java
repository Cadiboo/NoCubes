/*
 * Minecraft Forge
 * Copyright (c) 2016-2018.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.client.gui;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.IArrayEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.IConfigEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class bridges the gap between the FML config GUI classes and the Forge Configuration classes.
 */
public class ConfigElement {

	private final ForgeConfigSpec.ConfigValue<?> configValue;
	private boolean categoriesFirst = true;

	public ConfigElement(final ForgeConfigSpec.ConfigValue configValue) {
		this.configValue = configValue;
	}

	public static IConfigElement from(final Class<?> configClass) {
		return new IConfigElement() {
			@Override
			public boolean isProperty() {
				return false;
			}

			@Override
			public Class<? extends IConfigEntry> getConfigEntryClass() {
				return null;
			}

			@Override
			public Class<? extends IArrayEntry> getArrayEntryClass() {
				return null;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public String getQualifiedName() {
				return null;
			}

			@Override
			public String getLanguageKey() {
				return null;
			}

			@Override
			public String getComment() {
				return null;
			}

			@Override
			public List<IConfigElement> getChildElements() {
				return new ArrayList<>();
			}

			@Override
			public ConfigGuiType getType() {
				return null;
			}

			@Override
			public boolean isList() {
				return false;
			}

			@Override
			public boolean isListLengthFixed() {
				return false;
			}

			@Override
			public int getMaxListLength() {
				return 0;
			}

			@Override
			public boolean isDefault() {
				return false;
			}

			@Override
			public Object getDefault() {
				return null;
			}

			@Override
			public Object[] getDefaults() {
				return new Object[0];
			}

			@Override
			public void setToDefault() {

			}

			@Override
			public boolean requiresWorldRestart() {
				return false;
			}

			@Override
			public boolean showInGui() {
				return false;
			}

			@Override
			public boolean requiresMcRestart() {
				return false;
			}

			@Override
			public Object get() {
				return null;
			}

			@Override
			public Object[] getList() {
				return new Object[0];
			}

			@Override
			public void set(final Object value) {

			}

			@Override
			public void set(final Object[] aVal) {

			}

			@Override
			public String[] getValidValues() {
				return new String[0];
			}

			@Override
			public Object getMinValue() {
				return null;
			}

			@Override
			public Object getMaxValue() {
				return null;
			}

			@Override
			public Pattern getValidationPattern() {
				return null;
			}
		};
	}

//    public ConfigElement listCategoriesFirst(boolean categoriesFirst)
//    {
//        this.categoriesFirst = categoriesFirst;
//        return this;
//    }
//
//    public String getName()
//    {
//        return String.join(".", configValue.getPath());
//    }
//
//
//    @Override
//    public Class<? extends IConfigEntry> getConfigEntryClass()
//    {
//        return isProperty ? prop.getConfigEntryClass() : category.getConfigEntryClass();
//    }
//
//    @Override
//    public Class<? extends IArrayEntry> getArrayEntryClass()
//    {
//        return isProperty ? prop.getArrayEntryClass() : null;
//    }
//
//    @Override
//    public String getQualifiedName()
//    {
//        return isProperty ? prop.getName() : category.getQualifiedName();
//    }
//
//    @Override
//    public ConfigGuiType getType()
//    {
//        return isProperty ? getType(this.prop) : ConfigGuiType.CONFIG_CATEGORY;
//    }
//
//    public static ConfigGuiType getType(Property prop)
//    {
//        return prop.getType() == Property.Type.BOOLEAN ? ConfigGuiType.BOOLEAN : prop.getType() == Property.Type.DOUBLE ? ConfigGuiType.DOUBLE :
//            prop.getType() == Property.Type.INTEGER ? ConfigGuiType.INTEGER : prop.getType() == Property.Type.COLOR ? ConfigGuiType.COLOR :
//            prop.getType() == Property.Type.MOD_ID ? ConfigGuiType.MOD_ID : ConfigGuiType.STRING;
//    }
//
//    @Override
//    public boolean isList()
//    {
//        return isProperty && prop.isList();
//    }
//
//    @Override
//    public boolean isListLengthFixed()
//    {
//        return isProperty && prop.isListLengthFixed();
//    }
//
//    @Override
//    public int getMaxListLength()
//    {
//        return isProperty ? prop.getMaxListLength() : -1;
//    }
//
//    @Override
//    public String getComment()
//    {
//        return isProperty ? prop.getComment() : category.getComment();
//    }
//
//    @Override
//    public boolean isDefault()
//    {
//        return !isProperty || prop.isDefault();
//    }
//
//    @Override
//    public void setToDefault()
//    {
//        if (isProperty)
//            prop.setToDefault();
//    }
//
//    @Override
//    public boolean requiresWorldRestart()
//    {
//        return isProperty ? prop.requiresWorldRestart() : category.requiresWorldRestart();
//    }
//
//    @Override
//    public boolean showInGui()
//    {
//        return isProperty ? prop.showInGui() : category.showInGui();
//    }
//
//    @Override
//    public boolean requiresMcRestart()
//    {
//        return isProperty ? prop.requiresMcRestart() : category.requiresMcRestart();
//    }
//
//    @Override
//    public String[] getValidValues()
//    {
//        return isProperty ? prop.getValidValues() : null;
//    }
//
//    @Override
//    public String getLanguageKey()
//    {
//        return isProperty ? prop.getLanguageKey() : category.getLanguagekey();
//    }
//
//    @Override
//    public Object getDefault()
//    {
//        return isProperty ? prop.getDefault() : null;
//    }
//
//    @Override
//    public Object[] getDefaults()
//    {
//        if (isProperty)
//        {
//            String[] aVal = prop.getDefaults();
//            if (type == Property.Type.BOOLEAN)
//            {
//                Boolean[] ba = new Boolean[aVal.length];
//                for(int i = 0; i < aVal.length; i++)
//                    ba[i] = Boolean.valueOf(aVal[i]);
//                return ba;
//            }
//            else if (type == Property.Type.DOUBLE)
//            {
//                Double[] da = new Double[aVal.length];
//                for(int i = 0; i < aVal.length; i++)
//                    da[i] = Double.valueOf(aVal[i].toString());
//                return da;
//            }
//            else if (type == Property.Type.INTEGER)
//            {
//                Integer[] ia = new Integer[aVal.length];
//                for(int i = 0; i < aVal.length; i++)
//                    ia[i] = Integer.valueOf(aVal[i].toString());
//                return ia;
//            }
//            else
//                return aVal;
//        }
//        return null;
//    }
//
//    @Override
//    public Pattern getValidationPattern()
//    {
//        return isProperty ? prop.getValidationPattern() : null;
//    }
//
//    @Override
//    public Object get()
//    {
//        return isProperty ? prop.getString() : null;
//    }
//
//    @Override
//    public Object[] getList()
//    {
//        if (isProperty)
//        {
//            String[] aVal = prop.getStringList();
//            if (type == Property.Type.BOOLEAN)
//            {
//                Boolean[] ba = new Boolean[aVal.length];
//                for(int i = 0; i < aVal.length; i++)
//                    ba[i] = Boolean.valueOf(aVal[i]);
//                return ba;
//            }
//            else if (type == Property.Type.DOUBLE)
//            {
//                Double[] da = new Double[aVal.length];
//                for(int i = 0; i < aVal.length; i++)
//                    da[i] = Double.valueOf(aVal[i].toString());
//                return da;
//            }
//            else if (type == Property.Type.INTEGER)
//            {
//                Integer[] ia = new Integer[aVal.length];
//                for(int i = 0; i < aVal.length; i++)
//                    ia[i] = Integer.valueOf(aVal[i].toString());
//                return ia;
//            }
//            else
//                return aVal;
//        }
//        return null;
//    }
//
//    @Override
//    public void set(Object value)
//    {
//        if (isProperty)
//        {
//            if (type == Property.Type.BOOLEAN)
//                prop.set(Boolean.parseBoolean(value.toString()));
//            else if (type == Property.Type.DOUBLE)
//                prop.set(Double.parseDouble(value.toString()));
//            else if (type == Property.Type.INTEGER)
//                prop.set(Integer.parseInt(value.toString()));
//            else
//                prop.set(value.toString());
//        }
//    }
//
//    @Override
//    public void set(Object[] aVal)
//    {
//        if (isProperty)
//        {
//            if (type == Property.Type.BOOLEAN)
//            {
//                boolean[] ba = new boolean[aVal.length];
//                for(int i = 0; i < aVal.length; i++)
//                    ba[i] = Boolean.valueOf(aVal[i].toString());
//                prop.set(ba);
//            }
//            else if (type == Property.Type.DOUBLE)
//            {
//                double[] da = new double[aVal.length];
//                for(int i = 0; i < aVal.length; i++)
//                    da[i] = Double.valueOf(aVal[i].toString());
//                prop.set(da);
//            }
//            else if (type == Property.Type.INTEGER)
//            {
//                int[] ia = new int[aVal.length];
//                for(int i = 0; i < aVal.length; i++)
//                    ia[i] = Integer.valueOf(aVal[i].toString());
//                prop.set(ia);
//            }
//            else
//            {
//                String[] is = new String[aVal.length];
//                for(int i = 0; i < aVal.length; i++)
//                    is[i] = aVal[i].toString();
//                prop.set(is);
//            }
//        }
//    }
//
//    @Override
//    public Object getMinValue()
//    {
//        return isProperty ? prop.getMinValue() : null;
//    }
//
//    @Override
//    public Object getMaxValue()
//    {
//        return isProperty ? prop.getMaxValue() : null;
//    }
//
//    /**
//     * Provides a ConfigElement derived from the annotation-based config system
//     * @param configClass the class which contains the configuration
//     * @return A ConfigElement based on the described category.
//     */
//    public static IConfigElement from(Class<?> configClass)
//    {
////        Config annotation = configClass.getAnnotation(Config.class);
////        if (annotation == null)
////            throw new RuntimeException(String.format("The class '%s' has no @Config annotation!", configClass.getName()));
//
////        Configuration config = ConfigManager.getConfiguration(annotation.modid(), annotation.name());
////        if (config == null)
////        {
////            String error = String.format("The configuration '%s' of mod '%s' isn't loaded with the ConfigManager!", annotation.name(), annotation.modid());
////            throw new RuntimeException(error);
////        }
//
//        String name = Strings.isNullOrEmpty(annotation.name()) ? annotation.modid() : annotation.name();
//        String langKey = name;
//        Config.LangKey langKeyAnnotation = configClass.getAnnotation(Config.LangKey.class);
//        if (langKeyAnnotation != null)
//        {
//            langKey = langKeyAnnotation.value();
//        }
//
//        if (annotation.category().isEmpty())
//        {
//            List<IConfigElement> elements = Lists.newArrayList();
//            Set<String> catNames = config.getCategoryNames();
//            for (String catName : catNames)
//            {
//                if (catName.isEmpty())
//                    continue;
//                ConfigCategory category = config.getCategory(catName);
//                if (category.isChild())
//                    continue;
//                DummyCategoryElement element = new DummyCategoryElement(category.getName(), category.getLanguagekey(), new ConfigElement(category).getChildElements());
//                element.setRequiresMcRestart(category.requiresMcRestart());
//                element.setRequiresWorldRestart(category.requiresWorldRestart());
//                elements.add(element);
//            }
//
//            return new DummyCategoryElement(name, langKey, elements);
//        }
//        else
//        {
//            ConfigCategory category = config.getCategory(annotation.category());
//            DummyCategoryElement element = new DummyCategoryElement(name, langKey, new ConfigElement(category).getChildElements());
//            element.setRequiresMcRestart(category.requiresMcRestart());
//            element.setRequiresWorldRestart(category.requiresWorldRestart());
//            return element;
//        }
//    }
}
