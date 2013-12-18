// Copyright 2012 Cloudera Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.cloudera.impala.analysis;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Representation of a values() statement with a list of constant-expression lists.
 * ValuesStmt is a special case of a UnionStmt with the following restrictions:
 * - Operands are only constant selects
 * - Operands are connected by UNION ALL
 * - No nesting of ValuesStmts
 */
public class ValuesStmt extends UnionStmt {

  public ValuesStmt(List<UnionOperand> operands,
      ArrayList<OrderByElement> orderByElements, LimitElement limitElement) {
    super(operands, orderByElements, limitElement);
  }

  @Override
  protected String queryStmtToSql(QueryStmt queryStmt) {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("(");
    appendSelectList((SelectStmt) queryStmt, strBuilder);
    strBuilder.append(")");
    return strBuilder.toString();
  }

  @Override
  public String toSql() {
    StringBuilder strBuilder = new StringBuilder();

    if (withClause_ != null) {
      strBuilder.append(withClause_.toSql());
      strBuilder.append(" ");
    }

    Preconditions.checkState(operands_.size() > 0);
    strBuilder.append("VALUES(");
    for (int i = 0; i < operands_.size(); ++i) {
      if (operands_.size() != 1) strBuilder.append("(");
      appendSelectList((SelectStmt) operands_.get(i).getQueryStmt(), strBuilder);
      if (operands_.size() != 1) strBuilder.append(")");
      strBuilder.append((i+1 != operands_.size()) ? ", " : "");
    }
    strBuilder.append(")");
    return strBuilder.toString();
  }

  private void appendSelectList(SelectStmt select, StringBuilder strBuilder) {
    SelectList selectList = select.getSelectList();
    for (int j = 0; j < selectList.getItems().size(); ++j) {
      strBuilder.append(selectList.getItems().get(j).toSql());
      strBuilder.append((j+1 != selectList.getItems().size()) ? ", " : "");
    }
  }

  @Override
  public QueryStmt clone() {
    List<UnionOperand> operandClones = Lists.newArrayList();
    for (UnionOperand operand: operands_) {
      operandClones.add(operand.clone());
    }
    ValuesStmt valuesClone = new ValuesStmt(operandClones, cloneOrderByElements(),
        limitElement_ == null ? null : limitElement_.clone(null));
    valuesClone.setWithClause(cloneWithClause());
    return valuesClone;
  }
}
