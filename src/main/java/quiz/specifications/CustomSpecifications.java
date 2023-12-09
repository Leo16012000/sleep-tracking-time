package quiz.specifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.Metamodel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CustomSpecifications<T> {
    @PersistenceContext
    private EntityManager em;

    public CustomSpecifications() {
    }

    public Specification<T> customSpecificationBuilder(Map<String, Object> map) {
        return (root, query, builder) -> {
            query.distinct(true);
            List<Predicate> predicates = this.handleMap(builder, root, (Join)null, query, map, new ArrayList());
            return builder.and((Predicate[])predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public Specification<T> customSpecificationBuilder(Map<String, Object> map, List<String> includeOnlyFields) {
        return (root, query, builder) -> {
            query.distinct(true);
            List<Predicate> predicates = this.handleMap(builder, root, (Join)null, query, map, includeOnlyFields);
            return builder.and((Predicate[])predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public Specification<T> customSpecificationBuilder(List<Map<String, Object>> list) {
        return (root, query, builder) -> {
            query.distinct(true);
            List<Predicate> orPredicates = new ArrayList();
            Iterator var6 = list.iterator();

            while(var6.hasNext()) {
                Map<String, Object> map = (Map)var6.next();
                List<Predicate> predicates = this.handleMap(builder, root, (Join)null, query, map, new ArrayList());
                Predicate orPred = builder.and((Predicate[])predicates.toArray(new Predicate[predicates.size()]));
                orPredicates.add(orPred);
            }

            return builder.or((Predicate[])orPredicates.toArray(new Predicate[orPredicates.size()]));
        };
    }

    public List<Predicate> handleMap(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, Map<String, Object> map, List<String> includeOnlyFields) {
        if (join != null) {
            root = query.from(this.getJavaTypeOfClassContainingAttribute(root, join.getAttribute().getName()));
        }

        List<Predicate> predicates = new ArrayList();
        if (map.containsKey("q") && map.get("q") instanceof String) {
            predicates.add(this.searchInAllAttributesPredicate(builder, root, (String)map.get("q"), includeOnlyFields));
            map.remove("q");
        }

        Set<Attribute<? super T, ?>> attributes = root.getModel().getAttributes();
        Iterator var10 = map.entrySet().iterator();

        while(var10.hasNext()) {
            Map.Entry e = (Map.Entry)var10.next();
            String key = (String)e.getKey();
            Object val = e.getValue();
            String cleanKey = this.cleanUpKey(key);
            Attribute a = root.getModel().getAttribute(cleanKey);
            if (attributes.contains(a)) {
                Predicate pred = this.handleAllCases(builder, root, join, query, a, key, val);
                predicates.add(pred);
            }
        }

        return predicates;
    }

    public Predicate handleAllCases(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, Attribute a, String key, Object val) {
        boolean isValueCollection = val instanceof Collection;
        boolean isValueMap = val instanceof Map;
        String cleanKey = this.cleanUpKey(key);
        boolean isKeyClean = cleanKey.equals(key);
        boolean isNegation = key.endsWith("Not");
        boolean isGt = key.endsWith("Gt");
        boolean isGte = key.endsWith("Gte");
        boolean isLt = key.endsWith("Lt");
        boolean isLte = key.endsWith("Lte");
        boolean isConjunction = key.endsWith("And");
        boolean isAssociation = a.isAssociation();
        if (isValueMap) {
            val = this.convertIdValueToMap(val, a, root);
        }

        if (val instanceof Map && isAssociation) {
            List<Predicate> predicates = this.handleMap(builder, root, root.join(a.getName()), query, (Map)val, Arrays.asList());
            Predicate[] predicatesArray = (Predicate[])predicates.toArray(new Predicate[predicates.size()]);
            return builder.and(predicatesArray);
        } else if (isKeyClean) {
            return this.handleCleanKeyCase(builder, root, join, query, cleanKey, a, val);
        } else if (isNegation) {
            return builder.not(this.handleCleanKeyCase(builder, root, join, query, cleanKey, a, val));
        } else {
            if (isConjunction) {
                if (isValueCollection) {
                    return this.handleCollection(builder, root, join, query, a, cleanKey, (Collection)val, true);
                }
            } else {
                if (isLte) {
                    return this.createLtePredicate(builder, root, a, val);
                }

                if (isGte) {
                    return this.createGtePredicate(builder, root, a, val);
                }

                if (isLt) {
                    return this.createLtPredicate(builder, root, a, val);
                }

                if (isGt) {
                    return this.createGtPredicate(builder, root, a, val);
                }
            }

            return builder.conjunction();
        }
    }

    public Predicate handleCollection(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, Attribute a, String key, Collection values, boolean conjunction) {
        List<Predicate> predicates = new ArrayList();
        Iterator var10 = values.iterator();

        while(var10.hasNext()) {
            Object val = var10.next();
            Predicate pred = this.handleAllCases(builder, root, join, query, a, key, val);
            predicates.add(pred);
        }

        Predicate[] predicatesArray = (Predicate[])predicates.toArray(new Predicate[predicates.size()]);
        return conjunction ? builder.and(predicatesArray) : builder.or(predicatesArray);
    }

    public Predicate handleCleanKeyCase(CriteriaBuilder builder, Root root, Join join, CriteriaQuery query, String key, Attribute a, Object val) {
        boolean isValueCollection = val instanceof Collection;
        boolean isValTextSearch = val instanceof String && ((String)val).contains("%");
        if (isValueCollection) {
            return this.handleCollection(builder, root, join, query, a, key, (Collection)val, false);
        } else {
            return isValTextSearch ? this.createLikePredicate(builder, root, join, a, (String)val) : this.createEqualityPredicate(builder, root, join, a, val);
        }
    }

    public String getIdAttribute(EntityManager em, Class<T> clazz) {
        Metamodel m = em.getMetamodel();
        IdentifiableType<T> of = (IdentifiableType)m.managedType(clazz);
        return of.getId(of.getIdType().getJavaType()).getName();
    }

    private String cleanUpKey(String key) {
        List<String> postfixes = Arrays.asList("Gte", "Gt", "Lte", "Lt", "Not", "And");
        Iterator var3 = postfixes.iterator();

        String postfix;
        do {
            if (!var3.hasNext()) {
                return key;
            }

            postfix = (String)var3.next();
        } while(!key.endsWith(postfix));

        return key.substring(0, key.length() - postfix.length());
    }

    public Predicate searchInAllAttributesPredicate(CriteriaBuilder builder, Root root, String text, List<String> includeOnlyFields) {
        if (!text.contains("%")) {
            text = "%" + text + "%";
        }

        String finalText = text;
        Set<Attribute> attributes = root.getModel().getAttributes();
        List<Predicate> orPredicates = new ArrayList();
        Iterator var8 = attributes.iterator();

        while(var8.hasNext()) {
            Attribute a = (Attribute)var8.next();
            boolean javaTypeIsString = a.getJavaType().getSimpleName().equalsIgnoreCase("string");
            boolean shouldSearch = includeOnlyFields.isEmpty() || includeOnlyFields.contains(a.getName());
            if (javaTypeIsString && shouldSearch) {
                Predicate orPred = builder.like(root.get(a.getName()), finalText);
                orPredicates.add(orPred);
            }
        }

        return builder.or((Predicate[])orPredicates.toArray(new Predicate[orPredicates.size()]));
    }

    private Predicate createEqualityPredicate(CriteriaBuilder builder, Root root, Join join, Attribute a, Object val) {
        if (this.isNull(a, val)) {
            if (a.isAssociation() && a.isCollection()) {
                return builder.isEmpty(root.get(a.getName()));
            } else {
                return this.isPrimitive(a) ? builder.isNull(root.get(a.getName())) : root.get(a.getName()).isNull();
            }
        } else {
            if (join == null) {
                if (this.isEnum(a)) {
                    return builder.equal(root.get(a.getName()), Enum.valueOf((Class)Class.class.cast(a.getJavaType()), (String)val));
                }

                if (this.isPrimitive(a)) {
                    return builder.equal(root.get(a.getName()), val);
                }

                if (a.isAssociation()) {
                    return this.prepareJoinAssociatedPredicate(root, a, val);
                }
            } else if (join != null) {
                if (this.isEnum(a)) {
                    return builder.equal(join.get(a.getName()), Enum.valueOf((Class)Class.class.cast(a.getJavaType()), (String)val));
                }

                if (this.isPrimitive(a)) {
                    return builder.equal(join.get(a.getName()), val);
                }

                if (a.isAssociation()) {
                    return builder.equal(join.get(a.getName()), val);
                }
            }

            throw new IllegalArgumentException("equality/inequality is currently supported on primitives and enums");
        }
    }

    private Predicate createLikePredicate(CriteriaBuilder builder, Root<T> root, Join join, Attribute a, String val) {
        return join == null ? builder.like(root.get(a.getName()), val) : builder.like(join.get(a.getName()), val);
    }

    private Predicate createGtPredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            return builder.greaterThan(builder.lower(root.get(a.getName())), ((String)val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThan(root.get(a.getName()), (Integer)val);
        } else {
            throw new IllegalArgumentException("val type not supported yet");
        }
    }

    private Predicate createGtePredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            return builder.greaterThanOrEqualTo(builder.lower(root.get(a.getName())), ((String)val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.greaterThanOrEqualTo(root.get(a.getName()), (Integer)val);
        } else {
            throw new IllegalArgumentException("val type not supported yet");
        }
    }

    private Predicate createLtPredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            return builder.lessThan(builder.lower(root.get(a.getName())), ((String)val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThan(root.get(a.getName()), (Integer)val);
        } else {
            throw new IllegalArgumentException("val type not supported yet");
        }
    }

    private Predicate createLtePredicate(CriteriaBuilder builder, Root root, Attribute a, Object val) {
        if (val instanceof String) {
            return builder.lessThanOrEqualTo(builder.lower(root.get(a.getName())), ((String)val).toLowerCase());
        } else if (val instanceof Integer) {
            return builder.lessThanOrEqualTo(root.get(a.getName()), (Integer)val);
        } else {
            throw new IllegalArgumentException("val type not supported yet");
        }
    }

    private Predicate prepareJoinAssociatedPredicate(Root root, Attribute a, Object val) {
        Path rootJoinGetName = root.join(a.getName());
        Class referencedClass = rootJoinGetName.getJavaType();
        String referencedPrimaryKey = this.getIdAttribute(this.em, referencedClass);
        return rootJoinGetName.get(referencedPrimaryKey).in(new Object[]{val});
    }

    private Class getJavaTypeOfClassContainingAttribute(Root root, String attributeName) {
        Attribute a = root.getModel().getAttribute(attributeName);
        return a.isAssociation() ? root.join(a.getName()).getJavaType() : null;
    }

    private Object convertIdValueToMap(Object val, Attribute a, Root root) {
        Class javaTypeOfAttribute = this.getJavaTypeOfClassContainingAttribute(root, a.getName());
        String primaryKeyName = this.getIdAttribute(this.em, javaTypeOfAttribute);
        if (val instanceof Map && ((Map)val).keySet().size() == 1) {
            Map map = (Map)val;
            Iterator var7 = map.keySet().iterator();

            while(var7.hasNext()) {
                Object key = var7.next();
                if (key.equals(primaryKeyName)) {
                    return map.get(primaryKeyName);
                }
            }
        }

        return val;
    }

    private boolean isPrimitive(Attribute attribute) {
        String attributeJavaClass = attribute.getJavaType().getSimpleName().toLowerCase();
        return attributeJavaClass.startsWith("int") || attributeJavaClass.startsWith("long") || attributeJavaClass.equals("boolean") || attributeJavaClass.equals("string") || attributeJavaClass.equals("float") || attributeJavaClass.equals("double");
    }

    private boolean isEnum(Attribute attribute) {
        String parentJavaClass = "";
        if (attribute.getJavaType().getSuperclass() != null) {
            parentJavaClass = attribute.getJavaType().getSuperclass().getSimpleName().toLowerCase();
        }

        return parentJavaClass.equals("enum");
    }

    private boolean isNull(Attribute attribute, Object val) {
        if (this.isPrimitive(attribute)) {
            String attributeJavaClass = attribute.getJavaType().getSimpleName().toLowerCase();
            if (!attributeJavaClass.equals("string")) {
                return val == null;
            } else {
                String valObj = (String)val;
                return StringUtils.isBlank(valObj) || valObj.equalsIgnoreCase("null");
            }
        } else {
            return val == null;
        }
    }
}
