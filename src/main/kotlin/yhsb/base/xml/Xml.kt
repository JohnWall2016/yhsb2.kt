package yhsb.base.xml

import org.w3c.dom.NodeList as XmlNodeList
import org.w3c.dom.Element as XmlElement
import org.w3c.dom.NamedNodeMap as XmlNamedNodeMap
import org.w3c.dom.Document as XmlDocument
import org.xml.sax.InputSource
import yhsb.base.reflect.GenericClass
import yhsb.base.reflect.getAnnotation
import yhsb.base.structs.MapField
import java.io.StringReader
import java.io.StringWriter
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.TypeVariable
import java.math.BigDecimal
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.reflect.*
import kotlin.reflect.full.*

fun XmlNodeList.asSequence(): Sequence<XmlElement> = sequence {
    for (i in 0 until this@asSequence.length) {
        val item = this@asSequence.item(i)
        if (item is XmlElement)
            yield(item)
    }
}

fun XmlElement.getElementAttribute(
    tagName: String,
    attrName: String
): String? {
    getElementsByTagName(tagName).asSequence().forEach {
        val attr = it.getAttribute(attrName)
        if (attr != null && attr.isNotEmpty()) {
            return attr
        }
    }
    return null
}

fun XmlNamedNodeMap.asSequence(): Sequence<XmlElement> = sequence {
    for (i in 0 until this@asSequence.length) {
        val item = this@asSequence.item(i)
        if (item is XmlElement)
            yield(item)
    }
}

fun String.toXmlElement(): XmlElement = DocumentBuilderFactory.newInstance()
    .apply { isNamespaceAware = true }
    .newDocumentBuilder()
    .parse(InputSource(StringReader(this)))
    .documentElement

class DefaultFilter : (XmlElement) -> Boolean {
    override fun invoke(p1: XmlElement): Boolean = true
}

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class NS(val name: String, val value: String)

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Namespaces(val values: Array<NS> = [])

@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Node(val name: String = "", val filter: KClass<*> = DefaultFilter::class)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Attribute(val name: String = "")

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class AttrNode(val name: String, val attr: String = "")

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Text

val Class<*>.xmlFields: List<Field>
    get() {
        val jfields = mutableListOf<Field>()
        var jclass = this
        while (jclass != Object::class.java) {
            jclass.declaredFields.forEach {
                if (!Modifier.isTransient(it.modifiers) &&
                    it.annotations.size > 0 &&
                    (it.getAnnotation<Text>() != null ||
                            it.getAnnotation<Attribute>() != null ||
                            it.getAnnotation<Node>() != null ||
                            it.getAnnotation<AttrNode>() != null)
                ) jfields.add(it)
            }
            jclass = jclass.superclass as Class<*>
        }
        return jfields
    }

fun Namespaces.toMap(): Map<String, String> {
    return values.map { it.name to it.value }.toMap()
}

data class NodeStruct(
    val name: String,
    val node: ToXml,
    val namespaces: Map<String, String>?
)

fun XmlDocument.createElement(
    name: String,
    attributes: Map<String, String>,
    text: String? = null,
    namespaces: Map<String, String>? = null,
): XmlElement {
    val element = createElement(name)
    attributes.forEach {
        element.setAttribute(it.key, it.value)
    }
    namespaces?.forEach {
        element.setAttribute(it.key, it.value)
    }
    if (text != null) element.appendChild(createTextNode(text))
    return element
}

class SingleNode(val name: String, val attributes: Map<String, String>) : ToXml {
    override fun toXmlElement(doc: XmlDocument, nodeName: String?, namespaces: Map<String, String>?): XmlElement {
        return doc.createElement(name, attributes)
    }
}

interface ToXml {
    fun toXml(indent: Boolean = false): String {
        return DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .newDocument().apply {
                appendChild(toXmlElement(this@ToXml))
            }.transfromToString("""<?xml version="1.0" encoding="GBK"?>""", indent)
    }

    fun toXmlElement(doc: XmlDocument, nodeName: String?, namespaces: Map<String, String>? = null): XmlElement? =
        doc.toXmlElement(this@ToXml, nodeName, namespaces)
}

fun XmlDocument.transfromToString(declare: String? = null, indent: Boolean = false): String {
    return StringWriter().let {
        if (declare != null) it.write(declare)
        TransformerFactory.newInstance().newTransformer()
            .apply {
                if (declare != null) {
                    setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
                }
                setOutputProperty(OutputKeys.INDENT, if (indent) "yes" else "no")
            }.transform(DOMSource(this), StreamResult(it))
        it.buffer.toString()
    }
}

fun <T : Any> XmlDocument.toXmlElement(
    obj: T,
    nodeName: String? = null,
    namespaces: Map<String, String>? = null
): XmlElement? {
    val clazz = obj::class
    val nodeName = nodeName ?: clazz.findAnnotation<Node>()?.name ?: clazz.toString()
    val namespaces = namespaces ?: clazz.findAnnotation<Namespaces>()?.toMap() ?: mapOf()
    var text: String? = null

    val attrs = LinkedHashMap<String, String>()
    namespaces.forEach {
        val v = if (it.key.isEmpty()) "xmlns" else "xmlns:${it.key}"
        attrs[v] = it.value
    }

    val nodes = mutableListOf<NodeStruct>()

    clazz.java.xmlFields
        .forEach { field ->
            if (Modifier.isTransient(field.modifiers)) return@forEach
            field.setAccessible(true)
            val anno = field.getAnnotation<Text>()
            if (anno != null) {
                text = field.get(obj)?.toString()
            } else {
                val anno = field.getAnnotation<Attribute>()
                if (anno != null) {
                    val name = if (anno.name.isNotEmpty()) anno.name else field.name
                    attrs[name] = field.get(obj)?.toString() ?: ""
                } else {
                    val anno = field.getAnnotation<Node>()
                    if (anno != null) {
                        val name = if (anno.name.isNotEmpty()) anno.name else field.name
                        val node = field.get(obj)
                        if (node != null) {
                            if (node is List<*>) {
                                node.forEach {
                                    if (it is ToXml) {
                                        nodes.add(
                                            NodeStruct(
                                                name,
                                                it,
                                                field.getAnnotation<Namespaces>()?.toMap()
                                            )
                                        )
                                    }
                                }
                            } else if (node is ToXml) {
                                nodes.add(
                                    NodeStruct(
                                        name,
                                        node,
                                        field.getAnnotation<Namespaces>()?.toMap()
                                    )
                                )
                            }
                        }
                    } else {
                        val anno = field.getAnnotation<AttrNode>()
                        if (anno != null) {
                            val name = anno.name
                            val attr = if (anno.attr.isNotEmpty()) anno.attr else field.name
                            nodes.add(
                                NodeStruct(
                                    name,
                                    SingleNode(
                                        name,
                                        mapOf(
                                            attr to (field.get(obj)?.toString() ?: "")
                                        )
                                    ),
                                    field.getAnnotation<Namespaces>()?.toMap()
                                )
                            )
                        }
                    }
                }
            }
        }

    if (attrs.isNotEmpty() || text != null || nodes.isNotEmpty()) {
        return createElement(nodeName, attrs, text).apply {
            nodes.forEach {
                appendChild(it.node.toXmlElement(this@toXmlElement, it.name, it.namespaces))
            }
        }
    }

    return null
}

fun <T : Any> XmlElement.toObject(
    gClass: GenericClass<T>
): T {
    val obj = gClass.newInstance()

    obj::class.java.xmlFields.forEach { field ->
        field.setAccessible(true)
        val anno = field.getAnnotation<Text>()
        if (anno != null) {
            field.set(obj, textContent)
        } else {
            val anno = field.getAnnotation<Attribute>()
            if (anno != null) {
                val name = if (anno.name.isNotEmpty()) anno.name else field.name
                updateField(obj, field, getAttribute(name))
            } else {
                val anno = field.getAnnotation<AttrNode>()
                if (anno != null) {
                    val name = anno.name
                    val attr = if (anno.attr.isNotEmpty()) anno.attr else field.name
                    updateField(obj, field, getElementAttribute(name, attr))
                } else {
                    val anno = field.getAnnotation<Node>()
                    if (anno != null) {
                        val name = if (anno.name.isNotEmpty()) anno.name else field.name
                        @Suppress("UNCHECKED_CAST") val filter =
                            anno.filter.java.getConstructor().newInstance() as (XmlElement) -> Boolean
                        val elems = getElementsByTagName(name)
                        if (elems != null) {
                            updateField(obj, field, elems, gClass, filter)
                        }
                    }
                }
            }
        }
    }

    return obj
}

fun <T> updateField(obj: T, field: Field, value: String?) {
    val type = field.type
    if (type is Class<*> && MapField::class.java.isAssignableFrom(type)) {
        val v = type.getConstructor().newInstance() as MapField
        v.value = value ?: ""
        field.set(obj, v)
    } else if (type == Int::class.java) {
        field.set(obj, value?.toInt())
    } else if (type == BigDecimal::class.java) {
        field.set(obj, value?.toBigDecimal())
    } else {
        field.set(obj, value)
    }
}

fun <T : Any> updateField(
    obj: T,
    field: Field,
    elems: XmlNodeList,
    gClass: GenericClass<T>,
    filter: (XmlElement) -> Boolean
) {
    if (field.type == List::class.java) {
        val list = mutableListOf<Any>()
        val genericType = field.genericType
        if (genericType is ParameterizedType) {
            if (genericType.actualTypeArguments.size > 0) {
                val actualType = genericType.actualTypeArguments[0]
                var acutalClass: Class<*>? = null
                if (actualType is Class<*>) {
                    acutalClass = actualType
                } else if (actualType is TypeVariable<*>) {
                    acutalClass = gClass.resolveTypeParameter(actualType)
                }
                if (acutalClass != null) {
                    val childrenClass = gClass.createGenericClass<Any>(actualType)
                    elems.asSequence().filter(filter).forEach {
                        list.add(it.toObject(childrenClass))
                    }
                }
            }
        }
        field.set(obj, list)
    } else {
        elems.asSequence().find(filter)?.apply {
            field.set(obj, toObject(gClass.createGenericClass(field.genericType)))
        }
    }
}
