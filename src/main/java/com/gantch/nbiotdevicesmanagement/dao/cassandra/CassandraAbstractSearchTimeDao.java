package com.gantch.nbiotdevicesmanagement.dao.cassandra;

import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Ordering;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.gantch.nbiotdevicesmanagement.dao.BaseEntity;
import com.gantch.nbiotdevicesmanagement.dao.ModelConstants;
import com.gantch.nbiotdevicesmanagement.dao.page.TimePageLink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;


/**
 * Created by lcw332.
 */
public abstract class CassandraAbstractSearchTimeDao<E extends BaseEntity> extends CassandraAbstractModelDao<E> {

    protected List<E> findPageWithTimeSearch(String searchView, List<Clause> clauses, TimePageLink pageLink) {
        return findPageWithTimeSearch(searchView, clauses, Collections.emptyList(), pageLink);
    }

    protected List<E> findPageWithTimeSearch(String searchView, List<Clause> clauses, Ordering ordering, TimePageLink pageLink) {
        return findPageWithTimeSearch(searchView, clauses, Collections.singletonList(ordering), pageLink);
    }

    protected List<E> findPageWithTimeSearch(String searchView, List<Clause> clauses, List<Ordering> topLevelOrderings, TimePageLink pageLink) {
        return findPageWithTimeSearch(searchView, clauses, topLevelOrderings, pageLink, ModelConstants.ID_PROPERTY);
    }

    protected List<E> findPageWithTimeSearch(String searchView, List<Clause> clauses, TimePageLink pageLink, String idColumn) {
        return findPageWithTimeSearch(searchView, clauses, Collections.emptyList(), pageLink, idColumn);
    }

    protected List<E> findPageWithTimeSearch(String searchView, List<Clause> clauses, List<Ordering> topLevelOrderings, TimePageLink pageLink, String idColumn) {
        return findListByStatement(buildQuery(searchView, clauses, topLevelOrderings, pageLink, idColumn));
    }

    public static Select.Where buildQuery(String searchView, List<Clause> clauses, TimePageLink pageLink, String idColumn) {
        return buildQuery(searchView, clauses, Collections.emptyList(), pageLink, idColumn);
    }

    public static Select.Where buildQuery(String searchView, List<Clause> clauses, Ordering order, TimePageLink pageLink, String idColumn) {
        return buildQuery(searchView, clauses, Collections.singletonList(order), pageLink, idColumn);
    }

    public static Select.Where buildQuery(String searchView, List<Clause> clauses, List<Ordering> topLevelOrderings, TimePageLink pageLink, String idColumn) {
        Select select = select().from(searchView);
        Select.Where query = select.where();
        for (Clause clause : clauses) {
            query.and(clause);
        }
        query.limit(pageLink.getLimit());
        if (pageLink.isAscOrder()) {
            if (pageLink.getIdOffset() != null) {
                query.and(QueryBuilder.gt(idColumn, pageLink.getIdOffset()));
            } else if (pageLink.getStartTime() != null) {
                final UUID startOf = UUIDs.startOf(pageLink.getStartTime());
                query.and(QueryBuilder.gte(idColumn, startOf));
            }
            if (pageLink.getEndTime() != null) {
                final UUID endOf = UUIDs.endOf(pageLink.getEndTime());
                query.and(QueryBuilder.lte(idColumn, endOf));
            }
        } else {
            if (pageLink.getIdOffset() != null) {
                query.and(QueryBuilder.lt(idColumn, pageLink.getIdOffset()));
            } else if (pageLink.getEndTime() != null) {
                final UUID endOf = UUIDs.endOf(pageLink.getEndTime());
                query.and(QueryBuilder.lte(idColumn, endOf));
            }
            if (pageLink.getStartTime() != null) {
                final UUID startOf = UUIDs.startOf(pageLink.getStartTime());
                query.and(QueryBuilder.gte(idColumn, startOf));
            }
        }
        List<Ordering> orderings = new ArrayList<>(topLevelOrderings);
        if (pageLink.isAscOrder()) {
            orderings.add(QueryBuilder.asc(idColumn));
        } else {
            orderings.add(QueryBuilder.desc(idColumn));
        }
        query.orderBy(orderings.toArray(new Ordering[orderings.size()]));
        return query;
    }
}
