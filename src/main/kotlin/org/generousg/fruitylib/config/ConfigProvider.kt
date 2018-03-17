package org.generousg.fruitylib.config

import com.google.common.base.Preconditions
import com.google.common.base.Throwables
import com.google.common.collect.ImmutableSet
import net.minecraft.block.Block
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.registries.IForgeRegistry
import org.generousg.fruitylib.blocks.FruityBlock
import org.generousg.fruitylib.items.FruityItem
import org.generousg.fruitylib.util.Log
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField


class ConfigProvider() {
    private interface IAnnotationProcessor<in I, in A: Annotation> {
        fun process(block: I, annotation: A)
        fun getEntryName(annotation: A): String
        fun isEnabled(name: String): Boolean
    }

    private val NULL_FEATURE_MANAGER = object: AbstractFeatureManager() {
        override fun getCategories(): Set<String> = ImmutableSet.of()

        override fun getFeaturesInCategory(category: String): Set<String> = ImmutableSet.of()

        override fun isEnabled(category: String, name: String): Boolean = true
    }

    var features = NULL_FEATURE_MANAGER
    val blockFactory = FactoryRegistry<Block>()
    val itemFactory = FactoryRegistry<Item>()
    val itemBlockFactory = FactoryRegistry<ItemBlock>()
    var creativeTabs = hashMapOf<String, CreativeTabs>()

    private class IdDecorator(val joiner: String) {
        private var modId: String = ""

        fun setMod(modId: String) {
            this.modId = modId
        }

        fun decorate(id: String): String = modId + joiner + id
    }

    private val langDecorator = IdDecorator(".")
    private val genericDecorator = IdDecorator(":")

    private var modId: String = ""

    constructor(modPrefix: String, mainClass: KClass<*>) : this() {
        langDecorator.setMod(modPrefix)
        genericDecorator.setMod(modPrefix)

        creativeTabs = populateCreativeTabs(mainClass)

        val mod = Loader.instance().activeModContainer()
        checkNotNull(mod, {"This class can only be initialized in mod init"})
        this.modId = mod?.modId ?: "null"
    }

    private fun populateCreativeTabs(mainClass: KClass<*>): HashMap<String, CreativeTabs> {
        val result = hashMapOf<String, CreativeTabs>()
        mainClass.declaredMemberProperties
                .filter { CreativeTabs::class.java.isAssignableFrom(it.javaField?.type) }
                .forEach {
                    it.javaField?.isAccessible = true
                    result.put(it.name, it.javaField?.get(null) as CreativeTabs)
                }
        return result
    }

    fun setLanguageModId(modId: String) = langDecorator.setMod(modId)
    fun setGeneralModId(modId: String) = genericDecorator.setMod(modId)

    fun registerFluids(cls: Class<out FluidInstances>) {
        for(field in cls.fields) {
            if(Modifier.isStatic(field.modifiers) && Fluid::class.java.isAssignableFrom(field.type)) {
                if(field.isAnnotationPresent(IgnoreFeature::class.java)) continue
                val annotation = field.getAnnotation(RegisterFluid::class.java)
                if(annotation == null) {
                    Log.warn {"Field $field has valid type ${Fluid::class.java} for registration, but no annotation ${RegisterFluid::class.java}"}
                    continue
                }
                val name = annotation.name
                if(!features.isFluidEnabled(name)) {
                    Log.info {"Fluid $name (from field $field) is disabled"}
                    continue
                }
                val existing = FluidRegistry.getFluid(name)
                if(existing != null) {
                    field.set(null, existing)
                    continue
                }
                val new = field.get(null) as Fluid
                setFluidPrefixedId(annotation.unlocalizedName, name, langDecorator, { new.unlocalizedName = it })
                val result = new.setLuminosity(annotation.luminosity).setDensity(annotation.density).setViscosity(annotation.viscosity).setGaseous(annotation.gaseous).setTemperature(annotation.temperature)
                FluidRegistry.registerFluid(result)
                if(annotation.hasBucket) FluidRegistry.addBucketForFluid(result)

                field.set(null, result)
            }
        }
    }

    fun registerItems(clazz: Class<out ItemInstances>, registry: IForgeRegistry<Item>) {
        val items = mutableListOf<Item>()
        processAnnotations(clazz, Item::class.java, RegisterItem::class.java, itemFactory, object : IAnnotationProcessor<Item, RegisterItem> {
            override fun process(block: Item, annotation: RegisterItem) {
                val name = annotation.name
                registry.register(block)
                items.add(block.setRegistryName(name))
                if(annotation.creativeTab != "[none]" && creativeTabs[annotation.creativeTab] != null)
                    block.creativeTab = creativeTabs[annotation.creativeTab]
                setItemPrefixedId(annotation.unlocalizedName, name, langDecorator, { block.unlocalizedName = it})
                if(block is FruityItem) block.hasInfo = annotation.hasInfo
                if(FMLCommonHandler.instance().effectiveSide == Side.CLIENT) {
                    if(annotation.modelName == "[default]")
                        ModelLoader.setCustomModelResourceLocation(block, 0, ModelResourceLocation(block.registryName!!, "inventory"))
                    else ModelLoader.setCustomModelResourceLocation(block, 0, ModelResourceLocation(annotation.modelName, "inventory"))
                }
            }

            override fun getEntryName(annotation: RegisterItem): String = annotation.name
            override fun isEnabled(name: String): Boolean = features.isItemEnabled(name)
        })
        registry.registerAll(*items.toTypedArray())
    }

    fun registerBlocks(clazz: Class<out BlockInstances>, registry: IForgeRegistry<Block>) {
        val blocks = mutableListOf<Block>()
        val items = mutableListOf<Item>()
        processAnnotations(clazz, Block::class.java, RegisterBlock::class.java, blockFactory, object: IAnnotationProcessor<Block, RegisterBlock> {
            override fun process(block: Block, annotation: RegisterBlock) {
                val name = annotation.name
                val itemBlockClass = annotation.itemBlock
                val teClass = annotation.tileEntity

                blocks.add(block.setRegistryName(genericDecorator.decorate(name)))
                if(annotation.creativeTab != "[none]" && creativeTabs[annotation.creativeTab] != null)
                    block.setCreativeTab(creativeTabs[annotation.creativeTab]!!)
                setBlockPrefixedId(annotation.unlocalizedName, name, langDecorator, { block.unlocalizedName = it})

                val itemBlock = itemBlockFactory.constructItemBlock(name, itemBlockClass.java, block)
                items.add(itemBlock.setRegistryName(genericDecorator.decorate(name)))

                if(teClass != TileEntity::class) {
                    val teName = "te_$name"
                    GameRegistry.registerTileEntity(teClass.java, teName)
                    if(block is FruityBlock) block.teClass = teClass.java
                }

                for(te in annotation.tileEntities) {
                    GameRegistry.registerTileEntity(te.java, "te_${name}_${te.simpleName}")
                }

                if(block is IRegistrableBlock) block.setupBlock(modId, name, teClass, itemBlockClass)
                if(block is FruityBlock) block.hasInfo = annotation.hasInfo


                if(FMLCommonHandler.instance().effectiveSide == Side.CLIENT && block is FruityBlock) {
                    if(annotation.modelName == "[default]")
                        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, ModelResourceLocation(block.registryName!!, "inventory"))
                    else ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, ModelResourceLocation(annotation.modelName, "inventory"))
                }
            }

            override fun getEntryName(annotation: RegisterBlock): String = annotation.name
            override fun isEnabled(name: String): Boolean = features.isBlockEnabled(name)
        })

        registry.registerAll(*blocks.toTypedArray())
        ForgeRegistries.ITEMS.registerAll(*items.toTypedArray())
    }

    companion object Factory {
        private fun <I : Any, A : Annotation> processAnnotations(config: Class<out InstanceContainer<*>>, fieldClass: Class<I>, annotationClass: Class<A>, factory: FactoryRegistry<I>, processor: IAnnotationProcessor<I, A>) {
            for(field in config.fields) {
                if(Modifier.isStatic(field.modifiers) && fieldClass.isAssignableFrom(field.type)) {
                    if(field.isAnnotationPresent(IgnoreFeature::class.java)) continue
                    val annotation = field.getAnnotation(annotationClass)
                    if(annotation == null) {
                        Log.warn {"Field $field has valid type $fieldClass for registration, but no annotation $annotationClass"}
                        continue
                    }

                    val name = processor.getEntryName(annotation)
                    if(!processor.isEnabled(name)) {
                        Log.info {"Item $name (from field $field) is disabled"}
                        continue
                    }

                    @Suppress("UNCHECKED_CAST")
                    val fieldType = field.type as Class<out I>
                    val entry = factory.construct(name, fieldType)

                    try {
                        field.set(null, entry)
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }

                    processor.process(entry, annotation)
                }
            }
        }

        private fun setPrefixedId(id: String, objectName: String, decorator: IdDecorator, setter: (id: String) -> Unit, noneValue: String, defaultValue: String) {
            if(id != noneValue) {
                if(id == defaultValue) setter.invoke(decorator.decorate(objectName)) else setter.invoke(decorator.decorate(id))
            }
        }

        private fun setItemPrefixedId(id: String, itemName: String, decorator: IdDecorator, setter: (id: String) -> Unit) = setPrefixedId(id, itemName, decorator, setter, "[none]", "[default]")
        private fun setBlockPrefixedId(id: String, blockName: String, decorator: IdDecorator, setter: (id: String) -> Unit) = setPrefixedId(id, blockName, decorator, setter, "[none]", "[default]")
        private fun setFluidPrefixedId(id: String, fluidName: String, decorator: IdDecorator, setter: (id: String) -> Unit) = setPrefixedId(id, fluidName, decorator, setter, "[none]", "[default]")
    }
}
