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

import org.apache.hadoop.hive.metastore.MetaStoreUtils;

import com.cloudera.impala.catalog.Type;
import com.cloudera.impala.common.AnalysisException;
import com.cloudera.impala.thrift.TColumn;

/**
 * Represents a column definition in a CREATE/ALTER TABLE/VIEW statement.
 * Column definitions in CREATE/ALTER TABLE statements require a column type,
 * whereas column definitions in CREATE/ALTER VIEW statements infer the column type from
 * the corresponding view definition. All column definitions have an optional comment.
 * Since a column definition refers a column stored in the Metastore, the column name
 * must be valid according to the Metastore's rules (see @MetaStoreUtils).
 */
public class ColumnDesc {
  private final String colName_;
  private final String comment_;
  // Required in CREATE/ALTER TABLE stmts. Set to NULL in CREATE/ALTER VIEW stmts.
  private Type type_;

  public ColumnDesc(String colName, Type type, String comment) {
    colName_ = colName;
    type_ = type;
    comment_ = comment;
  }

  public void setType(Type type) { type_ = type; }
  public Type getType() { return type_; }
  public String getColName() { return colName_; }
  public String getComment() { return comment_; }

  public void analyze() throws AnalysisException {
    // Check whether the column name meets the Metastore's requirements.
    if (!MetaStoreUtils.validateName(colName_)) {
      throw new AnalysisException("Invalid column name: " + colName_);
    }
    type_.analyze();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(colName_);
    if (type_ != null) sb.append(" " + type_.toString());
    if (comment_ != null) sb.append(String.format(" COMMENT '%s'", comment_));
    return sb.toString();
  }

  public TColumn toThrift() {
    TColumn col = new TColumn(
        new TColumn(getColName(), type_.toThrift()));
    col.setComment(getComment());
    return col;
  }
}
