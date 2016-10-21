/*
 * Copyright (c) 2010-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.common.refinery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.common.ResourceObjectPattern;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.schema.SchemaRegistry;
import com.evolveum.midpoint.schema.ResourceShadowDiscriminator;
import com.evolveum.midpoint.schema.processor.*;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.midpoint.xml.ns._public.resource.capabilities_3.CapabilityType;
import com.evolveum.midpoint.xml.ns._public.resource.capabilities_3.PagedSearchCapabilityType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import org.jetbrains.annotations.NotNull;

/**
 * @author semancik
 * @author mederly
 *
 * Work in-progress.
 *
 */
public class LayerRefinedObjectClassDefinitionImpl implements LayerRefinedObjectClassDefinition {
	
	private RefinedObjectClassDefinition refinedObjectClassDefinition;
	private LayerType layer;
    /**
     * Keeps layer-specific information on resource object attributes.
     * This list is lazily evaluated.
     */
    private List<LayerRefinedAttributeDefinition<?>> layerRefinedAttributeDefinitions;
	
	private LayerRefinedObjectClassDefinitionImpl(RefinedObjectClassDefinition refinedAccountDefinition, LayerType layer) {
		this.refinedObjectClassDefinition = refinedAccountDefinition;
		this.layer = layer;
	}
	
	static LayerRefinedObjectClassDefinition wrap(RefinedObjectClassDefinition rAccountDef, LayerType layer) {
		if (rAccountDef == null) {
			return null;
		}
		return new LayerRefinedObjectClassDefinitionImpl(rAccountDef, layer);
	}
	
	static List<? extends LayerRefinedObjectClassDefinition> wrapCollection(Collection<? extends RefinedObjectClassDefinition> rAccountDefs, LayerType layer) {
		List<LayerRefinedObjectClassDefinition> outs = new ArrayList<LayerRefinedObjectClassDefinition>(rAccountDefs.size());
		for (RefinedObjectClassDefinition rAccountDef: rAccountDefs) {
			outs.add(wrap(rAccountDef, layer));
		}
		return outs;
	}

	@Override
	public LayerType getLayer() {
		return layer;
	}

    @NotNull
	@Override
	public QName getTypeName() {
		return refinedObjectClassDefinition.getTypeName();
	}

    @Override
	public boolean isIgnored() {
		return refinedObjectClassDefinition.isIgnored();
	}

    public boolean isEmphasized() {
		return refinedObjectClassDefinition.isEmphasized();
	}

	@Override
    public ResourceAttributeDefinition<?> getDescriptionAttribute() {
        return substituteLayerRefinedAttributeDefinition(refinedObjectClassDefinition.getDescriptionAttribute());
    }

	@Override
	@NotNull
	public List<String> getIgnoredNamespaces() {
		return refinedObjectClassDefinition.getIgnoredNamespaces();
	}

	private LayerRefinedAttributeDefinition<?> substituteLayerRefinedAttributeDefinition(ResourceAttributeDefinition<?> attributeDef) {
        LayerRefinedAttributeDefinition<?> rAttrDef = findAttributeDefinition(attributeDef.getName());
        return rAttrDef;
    }

    private Collection<LayerRefinedAttributeDefinition<?>> substituteLayerRefinedAttributeDefinitionCollection(Collection<? extends RefinedAttributeDefinition<?>> attributes) {
        Collection<LayerRefinedAttributeDefinition<?>> retval = new ArrayList<>();
        for (RefinedAttributeDefinition<?> rad : attributes) {
            retval.add(substituteLayerRefinedAttributeDefinition(rad));
        }
        return retval;
    }

    @Override
    public LayerRefinedAttributeDefinition<?> getNamingAttribute() {
        return substituteLayerRefinedAttributeDefinition(refinedObjectClassDefinition.getNamingAttribute());
	}

    @Override
	public String getNativeObjectClass() {
		return refinedObjectClassDefinition.getNativeObjectClass();
	}

	@Override
	public <ID extends ItemDefinition> ID findNamedItemDefinition(@NotNull QName firstName, @NotNull ItemPath rest,
			@NotNull Class<ID> clazz) {
		return refinedObjectClassDefinition.findNamedItemDefinition(firstName, rest, clazz);
	}

	@Override
	public boolean isAuxiliary() {
		return refinedObjectClassDefinition.isAuxiliary();
	}

	@Override
	public Integer getDisplayOrder() {
		return refinedObjectClassDefinition.getDisplayOrder();
	}

	@Override
	public <ID extends ItemDefinition> ID findItemDefinition(@NotNull ItemPath path,
			@NotNull Class<ID> clazz) {
		return refinedObjectClassDefinition.findItemDefinition(path, clazz);
	}

	@Override
	public boolean isDefaultInAKind() {
		return refinedObjectClassDefinition.isDefaultInAKind();
	}

    @Override
	public ShadowKindType getKind() {
		return refinedObjectClassDefinition.getKind();
	}

	@Override
	public AttributeFetchStrategyType getPasswordFetchStrategy() {
		return refinedObjectClassDefinition.getPasswordFetchStrategy();
	}

    @Override
	public String getIntent() {
		return refinedObjectClassDefinition.getIntent();
	}

	@Override
	public LayerRefinedObjectClassDefinition forLayer(LayerType layerType) {
		return refinedObjectClassDefinition.forLayer(layerType);
	}

	@Override
	public LayerRefinedAttributeDefinition<?> getDisplayNameAttribute() {
        return substituteLayerRefinedAttributeDefinition(refinedObjectClassDefinition.getDisplayNameAttribute());
	}

	@Override
	public String getHelp() {
		return refinedObjectClassDefinition.getHelp();
	}

    @Override
	public Collection<? extends LayerRefinedAttributeDefinition<?>> getPrimaryIdentifiers() {
        return substituteLayerRefinedAttributeDefinitionCollection(refinedObjectClassDefinition.getPrimaryIdentifiers());
	}
    
    @Override
	public Collection<? extends LayerRefinedAttributeDefinition<?>> getAllIdentifiers() {
        return substituteLayerRefinedAttributeDefinitionCollection(refinedObjectClassDefinition.getAllIdentifiers());
	}

    @Override
	public Collection<? extends LayerRefinedAttributeDefinition<?>> getSecondaryIdentifiers() {
		return LayerRefinedAttributeDefinitionImpl.wrapCollection(refinedObjectClassDefinition.getSecondaryIdentifiers(), layer);
	}

    @Override
	public Class getTypeClass() {
		return refinedObjectClassDefinition.getTypeClass();
	}

    @Override
	public Collection<ResourceObjectPattern> getProtectedObjectPatterns() {
		return refinedObjectClassDefinition.getProtectedObjectPatterns();
	}

	@Override
	public void merge(ComplexTypeDefinition otherComplexTypeDef) {
		refinedObjectClassDefinition.merge(otherComplexTypeDef);
	}

	@Override
	public PrismContext getPrismContext() {
		return refinedObjectClassDefinition.getPrismContext();
	}

	@Override
	public ResourceAttributeContainer instantiate(QName name) {
		return refinedObjectClassDefinition.instantiate(name);
	}

    @Override
	public PrismPropertyDefinition findPropertyDefinition(@NotNull QName name) {
        LayerRefinedAttributeDefinition def = findAttributeDefinition(name);
        if (def != null) {
            return def;
        } else {
            // actually, can there be properties other than attributes? [mederly]
		    return LayerRefinedAttributeDefinitionImpl.wrap((RefinedAttributeDefinition) refinedObjectClassDefinition.findPropertyDefinition(name), layer);
        }
	}

    @Override
	public <X> LayerRefinedAttributeDefinition<X> findAttributeDefinition(QName elementQName) {
        for (LayerRefinedAttributeDefinition definition : getAttributeDefinitions()) {
            if (QNameUtil.match(definition.getName(), elementQName)) {
                return definition;
            }
        }
        return null;
	}

    @Override
	public <X> LayerRefinedAttributeDefinition<X> findAttributeDefinition(String elementLocalname) {
		return findAttributeDefinition(new QName(getResourceNamespace(), elementLocalname));        // todo or should we use ns-less matching?
	}

    @Override
	public String getDisplayName() {
		return refinedObjectClassDefinition.getDisplayName();
	}

    @Override
	public List<? extends ItemDefinition> getDefinitions() {
		return getAttributeDefinitions();
	}

    @Override
	public String getDescription() {
		return refinedObjectClassDefinition.getDescription();
	}

    @Override
	public boolean isDefault() {
		return refinedObjectClassDefinition.isDefault();
	}

    @Override
	public ObjectClassComplexTypeDefinition getObjectClassDefinition() {
		return refinedObjectClassDefinition.getObjectClassDefinition();
	}

    @Override
	public List<? extends LayerRefinedAttributeDefinition<?>> getAttributeDefinitions() {
        if (layerRefinedAttributeDefinitions == null) {
            layerRefinedAttributeDefinitions = LayerRefinedAttributeDefinitionImpl.wrapCollection(refinedObjectClassDefinition.getAttributeDefinitions(), layer);
        }
		return layerRefinedAttributeDefinitions;
	}

	@Override
	public boolean containsAttributeDefinition(ItemPathType pathType) {
		return refinedObjectClassDefinition.containsAttributeDefinition(pathType);
	}

	@Override
    public ResourceType getResourceType() {
		return refinedObjectClassDefinition.getResourceType();
	}

    @Override
	public PrismObjectDefinition<ShadowType> getObjectDefinition() {
		return refinedObjectClassDefinition.getObjectDefinition();
	}

    @Override
	public LayerRefinedAttributeDefinition<?> getAttributeDefinition(QName attributeName) {
        // todo should there be any difference between findAttributeDefinition and getAttributeDefinition? [mederly]
		return findAttributeDefinition(attributeName);
	}

    @Override
	public boolean containsAttributeDefinition(QName attributeName) {
		return refinedObjectClassDefinition.containsAttributeDefinition(attributeName);
	}

    @Override
	public boolean isEmpty() {
		return refinedObjectClassDefinition.isEmpty();
	}

    @Override
	public PrismObject<ShadowType> createBlankShadow() {
		return refinedObjectClassDefinition.createBlankShadow();
	}

    @Override
	public ResourceShadowDiscriminator getShadowDiscriminator() {
		return refinedObjectClassDefinition.getShadowDiscriminator();
	}

    @Override
	public Collection<? extends QName> getNamesOfAttributesWithOutboundExpressions() {
		return refinedObjectClassDefinition.getNamesOfAttributesWithOutboundExpressions();
	}

    @Override
    public Collection<? extends QName> getNamesOfAttributesWithInboundExpressions() {
		return refinedObjectClassDefinition.getNamesOfAttributesWithInboundExpressions();
	}

    @Override
	public List<MappingType> getPasswordInbound() {
		return refinedObjectClassDefinition.getPasswordInbound();
	}

    @Override
	public MappingType getPasswordOutbound() {
		return refinedObjectClassDefinition.getPasswordOutbound();
	}

    @Override
	public ObjectReferenceType getPasswordPolicy() {
		return refinedObjectClassDefinition.getPasswordPolicy();
	}

	@Override
	public ResourcePasswordDefinitionType getPasswordDefinition() {
		return refinedObjectClassDefinition.getPasswordDefinition();
	}

	@Override
	public Class<?> getCompileTimeClass() {
		return refinedObjectClassDefinition.getCompileTimeClass();
	}

    @Override
	public QName getExtensionForType() {
		return refinedObjectClassDefinition.getExtensionForType();
	}

    @Override
    public boolean isContainerMarker() {
		return refinedObjectClassDefinition.isContainerMarker();
	}

    @Override
	public boolean isPrimaryIdentifier(QName attrName) {
		return refinedObjectClassDefinition.isPrimaryIdentifier(attrName);
	}

	@Override
	public boolean isInherited() {
		return refinedObjectClassDefinition.isInherited();
	}

	@Override
	public boolean isObjectMarker() {
		return refinedObjectClassDefinition.isObjectMarker();
	}

    @Override
	public boolean isXsdAnyMarker() {
		return refinedObjectClassDefinition.isXsdAnyMarker();
	}

    @Override
	public QName getSuperType() {
		return refinedObjectClassDefinition.getSuperType();
	}

    @Override
	public boolean isSecondaryIdentifier(QName attrName) {
		return refinedObjectClassDefinition.isSecondaryIdentifier(attrName);
	}

    @Override
	public boolean isRuntimeSchema() {
		return refinedObjectClassDefinition.isRuntimeSchema();
	}

    @Override
	public Collection<RefinedAssociationDefinition> getAssociations() {
		return refinedObjectClassDefinition.getAssociations();
	}

    @Override
	public Collection<RefinedAssociationDefinition> getAssociations(ShadowKindType kind) {
		return refinedObjectClassDefinition.getAssociations(kind);
	}

	@Override
	public <X> ResourceAttributeDefinition<X> findAttributeDefinition(QName name, boolean caseInsensitive) {
		return refinedObjectClassDefinition.findAttributeDefinition(name, caseInsensitive);
	}

	@Override
	public Collection<QName> getNamesOfAssociations() {
		return refinedObjectClassDefinition.getNamesOfAssociations();
	}

    @Override
    public ResourceActivationDefinitionType getActivationSchemaHandling() {
		return refinedObjectClassDefinition.getActivationSchemaHandling();
	}

    @Override
	public ResourceBidirectionalMappingType getActivationBidirectionalMappingType(QName propertyName) {
		return refinedObjectClassDefinition.getActivationBidirectionalMappingType(propertyName);
	}

    @Override
	public AttributeFetchStrategyType getActivationFetchStrategy(QName propertyName) {
		return refinedObjectClassDefinition.getActivationFetchStrategy(propertyName);
	}

	@Override
	public <T extends CapabilityType> T getEffectiveCapability(
			Class<T> capabilityClass) {
		return refinedObjectClassDefinition.getEffectiveCapability(capabilityClass);
	}

	@Override
	public PagedSearchCapabilityType getPagedSearches() {
		return refinedObjectClassDefinition.getPagedSearches();
	}

	@Override
	public boolean isPagedSearchEnabled() {
		return refinedObjectClassDefinition.isPagedSearchEnabled();
	}

	@Override
	public boolean isObjectCountingEnabled() {
		return refinedObjectClassDefinition.isObjectCountingEnabled();
	}

	@Override
	public Collection<RefinedAssociationDefinition> getEntitlementAssociations() {
		return refinedObjectClassDefinition.getEntitlementAssociations();
	}

    @Override
	public boolean isAbstract() {
		return refinedObjectClassDefinition.isAbstract();
	}

    @Override
	public boolean isDeprecated() {
		return refinedObjectClassDefinition.isDeprecated();
	}

    @Override
	public String getDocumentation() {
		return refinedObjectClassDefinition.getDocumentation();
	}

    @Override
	public String getDocumentationPreview() {
		return refinedObjectClassDefinition.getDocumentationPreview();
	}

    @Override
	public RefinedAssociationDefinition findAssociation(QName name) {
		return refinedObjectClassDefinition.findAssociation(name);
	}

	@Override
	public <ID extends ItemDefinition> ID findItemDefinition(@NotNull QName name, @NotNull Class<ID> clazz,
			boolean caseInsensitive) {
		ID def = refinedObjectClassDefinition.findItemDefinition(name, clazz, caseInsensitive);
		return (ID) LayerRefinedAttributeDefinitionImpl.wrap((RefinedAttributeDefinition) def, layer);
	}

	@Override
	public RefinedAssociationDefinition findEntitlementAssociation(QName name) {
		return refinedObjectClassDefinition.findEntitlementAssociation(name);
	}

    @Override
	public Collection<? extends QName> getNamesOfAssociationsWithOutboundExpressions() {
		return refinedObjectClassDefinition.getNamesOfAssociationsWithOutboundExpressions();
	}

    @Override
	public boolean matches(ShadowType shadowType) {
		return refinedObjectClassDefinition.matches(shadowType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((layer == null) ? 0 : layer.hashCode());
		result = prime * result + ((refinedObjectClassDefinition == null) ? 0 : refinedObjectClassDefinition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
//		if (!super.equals(obj))
//			return false;
		if (getClass() != obj.getClass())
			return false;
		LayerRefinedObjectClassDefinitionImpl other = (LayerRefinedObjectClassDefinitionImpl) obj;
		if (layer != other.layer)
			return false;
		if (refinedObjectClassDefinition == null) {
			if (other.refinedObjectClassDefinition != null)
				return false;
		} else if (!refinedObjectClassDefinition.equals(other.refinedObjectClassDefinition))
			return false;
		return true;
	}
	
	@Override
	public String debugDump() {
		return debugDump(0);
	}

	@Override
	public String getDefaultNamespace() {
		return refinedObjectClassDefinition.getDefaultNamespace();
	}

	@Override
	public String debugDump(int indent) {
		// TODO fix this hack
		return ((RefinedObjectClassDefinitionImpl) refinedObjectClassDefinition).debugDump(indent, layer, getDebugDumpClassName());
	}

    // Do NOT override&delegate debugDump(int indent, LayerType layer) here.
    // We want to use code in the context of this class so things like 
    // getDebugDumpClassName() will be correct.
	
	/**
     * Return a human readable name of this class suitable for logs.
     */
    protected String getDebugDumpClassName() {
        return "LRObjectClassDef";
    }

    @Override
	public String getHumanReadableName() {
		return refinedObjectClassDefinition.getHumanReadableName();
	}

    @Override
    public LayerRefinedObjectClassDefinition clone() {
        return wrap(refinedObjectClassDefinition.clone(), this.layer);
    }

    @Override
	public String getResourceNamespace() {
        return refinedObjectClassDefinition.getResourceNamespace();
    }

    @Override
    public ResourceObjectReferenceType getBaseContext() {
		return refinedObjectClassDefinition.getBaseContext();
	}

	@Override
	public Class getTypeClassIfKnown() {
		return refinedObjectClassDefinition.getTypeClassIfKnown();
	}

	@Override
	public Collection<RefinedObjectClassDefinition> getAuxiliaryObjectClassDefinitions() {
		return refinedObjectClassDefinition.getAuxiliaryObjectClassDefinitions();
	}

	@Override
	public boolean hasAuxiliaryObjectClass(QName expectedObjectClassName) {
		return refinedObjectClassDefinition.hasAuxiliaryObjectClass(expectedObjectClassName);
	}

	@Override
    public ObjectQuery createShadowSearchQuery(String resourceOid) throws SchemaException {
    	return refinedObjectClassDefinition.createShadowSearchQuery(resourceOid);
    }

	@Override
	public void revive(PrismContext prismContext) {
		refinedObjectClassDefinition.revive(prismContext);
	}
}
