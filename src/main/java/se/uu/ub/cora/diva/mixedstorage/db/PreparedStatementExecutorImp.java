/*
 * Copyright 2020 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.diva.mixedstorage.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import se.uu.ub.cora.sqldatabase.DatabaseFacade;

public class PreparedStatementExecutorImp implements StatementExecutor {

	@Override
	public void executeDbStatmentUsingDatabaseFacade(List<DbStatement> dbStatements,
			DatabaseFacade databaseFacade) {
		for (DbStatement dbStatement : dbStatements) {
			StringBuilder sql = createSql(dbStatement);
			// try {
			tryToExecuteUsingPreparedStatement(dbStatement, sql, databaseFacade);
			// } catch (SQLException e) {
			// throw SqlStorageException
			// .withMessageAndException("Error executing statement: " + sql, e);
			// }
		}

	}

	private void tryToExecuteUsingPreparedStatement(DbStatement dbStatement, StringBuilder sql,
			DatabaseFacade databaseFacade) {
		List<Object> parameterValues = getAllValuesAndConditions(dbStatement);
		databaseFacade.executeSqlWithValues(sql.toString(), parameterValues);

	}

	private List<Object> getAllValuesAndConditions(DbStatement dbStatement) {
		return addColumnsAndConditionsToValuesForUpdate(dbStatement.getValues(),
				dbStatement.getConditions());
	}

	private StringBuilder createSql(DbStatement dbStatement) {
		StringBuilder sql = new StringBuilder();
		if ("delete".equals(dbStatement.getOperation())) {
			sql.append(createSqlForDelete(dbStatement.getTableName(), dbStatement.getConditions()));
		} else if ("update".equals(dbStatement.getOperation())) {
			sql.append(createSqlForUpdate(dbStatement.getTableName(), dbStatement.getValues(),
					dbStatement.getConditions()));
		} else {
			sql.append(createSqlForInsert(dbStatement.getTableName(), dbStatement.getValues()));

		}
		return sql;
	}

	private StringBuilder createSqlForUpdate(String tableName,
			Map<String, Object> columnsWithValues, Map<String, Object> conditions) {
		StringBuilder sql = new StringBuilder(
				createSettingPartOfSqlStatement(tableName, columnsWithValues));
		if (!conditions.isEmpty()) {
			sql.append(createWherePartOfSqlStatement(conditions));
		}
		return sql;
	}

	private String createSettingPartOfSqlStatement(String tableName,
			Map<String, Object> columnsWithValues) {
		StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
		List<String> columnNames = getAllColumnNames(columnsWithValues);
		return appendColumnsToSelectPart(sql, columnNames);
	}

	private String appendColumnsToSelectPart(StringBuilder sql, List<String> columnNames) {
		StringJoiner joiner = new StringJoiner(", ");
		addAllToJoiner(columnNames, joiner);
		sql.append(joiner);
		return sql.toString();
	}

	private void addAllToJoiner(List<String> columnNames, StringJoiner joiner) {
		for (String columnName : columnNames) {
			joiner.add(columnName + " = ?");
		}
	}

	private List<String> getAllColumnNames(Map<String, Object> columnsWithValues) {
		List<String> columnNames = new ArrayList<>(columnsWithValues.size());
		for (Entry<String, Object> column : columnsWithValues.entrySet()) {
			columnNames.add(column.getKey());
		}
		return columnNames;
	}

	private String createWherePartOfSqlStatement(Map<String, Object> conditions) {
		StringBuilder sql = new StringBuilder(" WHERE ");
		List<String> conditionNames = getAllConditionNames(conditions);
		return appendConditionsToWherePart(sql, conditionNames);
	}

	private List<String> getAllConditionNames(Map<String, Object> conditions) {
		List<String> conditionNames = new ArrayList<>(conditions.size());
		for (Entry<String, Object> condition : conditions.entrySet()) {
			conditionNames.add(condition.getKey());
		}
		return conditionNames;
	}

	private String appendConditionsToWherePart(StringBuilder sql, List<String> conditions) {
		StringJoiner joiner = new StringJoiner(" AND ");
		addAllToJoiner(conditions, joiner);
		sql.append(joiner);
		return sql.toString();
	}

	private List<Object> addColumnsAndConditionsToValuesForUpdate(Map<String, Object> columns,
			Map<String, Object> conditions) {
		List<Object> valuesForUpdate = new ArrayList<>();
		valuesForUpdate.addAll(columns.values());
		valuesForUpdate.addAll(conditions.values());
		return valuesForUpdate;
	}

	private StringBuilder createSqlForDelete(String tableName, Map<String, Object> conditions) {
		StringBuilder sql = new StringBuilder("DELETE FROM " + tableName + " WHERE ");
		List<String> allConditionNames = getAllConditionNames(conditions);
		appendConditionsToWherePart(sql, allConditionNames);
		return sql;
	}

	private StringBuilder createSqlForInsert(String tableName,
			Map<String, Object> columnsWithValues) {
		StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + "(");
		List<String> columnNames = getAllColumnNames(columnsWithValues);
		appendColumnNamesToInsertPart(sql, columnNames);
		appendValuesPart(sql, columnNames);
		return sql;
	}

	private String appendColumnNamesToInsertPart(StringBuilder sql, List<String> columnNames) {
		StringJoiner joiner = new StringJoiner(", ");
		addAllToInsertJoiner(columnNames, joiner);
		sql.append(joiner);
		return sql.toString();
	}

	private void addAllToInsertJoiner(List<String> columnNames, StringJoiner joiner) {
		for (String columnName : columnNames) {
			joiner.add(columnName);
		}
	}

	private void appendValuesPart(StringBuilder sql, List<String> columnNames) {
		sql.append(") VALUES(");
		sql.append(addCorrectNumberOfPlaceHoldersForValues(columnNames));
		sql.append(')');
	}

	private String addCorrectNumberOfPlaceHoldersForValues(List<String> columnNames) {
		StringJoiner joiner = new StringJoiner(", ");
		for (int i = 0; i < columnNames.size(); i++) {
			joiner.add("?");
		}
		return joiner.toString();
	}

}
