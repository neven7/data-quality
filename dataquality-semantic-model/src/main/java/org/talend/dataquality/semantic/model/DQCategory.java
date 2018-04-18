// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.semantic.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DQCategory implements Serializable {

    private static final long serialVersionUID = 4593691452129397269L;

    private String id;

    private String name;

    private String label;

    private String description;

    private CategoryType type; // A type: RE, DD, KW (needed? How to manage OR clause: RE or in DD?)

    private CategoryPrivacyLevel privacyLevel;

    private String version;

    private DQUser creator;

    private Date createdAt;

    private DQRegEx regEx;

    private Date modifiedAt;

    private DQUser lastModifier;

    private Boolean completeness;

    private Date publishedAt;

    private DQUser lastPublisher;

    private ValidationMode validationMode;

    private CategoryState state;

    private List<DQCategory> children;

    private List<DQCategory> parents;

    @JsonIgnore
    private Boolean modified = Boolean.FALSE;

    @JsonIgnore
    private Boolean deleted = Boolean.FALSE;

    public DQCategory(String id) {
        this.id = id;
    }

    public DQCategory() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public CategoryPrivacyLevel getPrivacyLevel() {
        return privacyLevel;
    }

    public void setPrivacyLevel(CategoryPrivacyLevel privacyLevel) {
        this.privacyLevel = privacyLevel;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public DQUser getCreator() {
        return creator;
    }

    public void setCreator(DQUser creator) {
        this.creator = creator;
    }

    public DQRegEx getRegEx() {
        return regEx;
    }

    public void setRegEx(DQRegEx regEx) {
        this.regEx = regEx;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCompleteness() {
        return completeness;
    }

    public void setCompleteness(Boolean completeness) {
        this.completeness = completeness;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public DQUser getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(DQUser lastModifier) {
        this.lastModifier = lastModifier;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public DQUser getLastPublisher() {
        return lastPublisher;
    }

    public void setLastPublisher(DQUser lastPublisher) {
        this.lastPublisher = lastPublisher;
    }

    public CategoryState getState() {
        return state;
    }

    public void setState(CategoryState state) {
        this.state = state;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<DQCategory> getChildren() {
        return children;
    }

    public void setChildren(List<DQCategory> children) {
        this.children = children;
    }

    public List<DQCategory> getParents() {
        return parents;
    }

    public void setParents(List<DQCategory> parents) {
        this.parents = parents;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    public Boolean getModified() {
        return modified;
    }

    public void setModified(Boolean modified) {
        this.modified = modified;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return String.format(
                "Category [ID=%s  Type=%s  Name=%-20s  Label=%-20s  Completeness=%s  Modified=%-5s  Creator=%s Last Modifier=%s State=%-20s Last published=%s]",
                id, type, name, label, completeness, modified, creator, lastModifier, state, publishedAt);
    }

}
