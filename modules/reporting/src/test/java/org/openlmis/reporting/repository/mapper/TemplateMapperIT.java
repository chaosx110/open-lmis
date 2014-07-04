/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2013 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.reporting.repository.mapper;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openlmis.core.domain.Right;
import org.openlmis.core.domain.RightType;
import org.openlmis.core.domain.Role;
import org.openlmis.core.query.QueryExecutor;
import org.openlmis.core.repository.mapper.RightMapper;
import org.openlmis.core.repository.mapper.RoleRightsMapper;
import org.openlmis.db.categories.IntegrationTests;
import org.openlmis.reporting.model.Template;
import org.openlmis.reporting.model.TemplateParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTests.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-applicationContext-reporting.xml")
@Transactional
@TransactionConfiguration(defaultRollback = true, transactionManager = "openLmisTransactionManager")
public class TemplateMapperIT {

  public static final byte[] DATA = new byte[1];
  public static final long CREATED_BY = 1L;
  public static final String DESCRIPTION = "description";

  @Autowired
  TemplateMapper mapper;

  @Autowired
  ReportRightMapper reportRightMapper;

  @Autowired
  RightMapper rightMapper;

  @Autowired
  RoleRightsMapper roleRightsMapper;

  @Autowired
  QueryExecutor queryExecutor;

  @Test
  public void shouldGetById() {
    Template template = createReportTemplate("Sample Report", "Consistency Report");

    Template returnedTemplate = mapper.getById(template.getId());

    assertThat(returnedTemplate.getName(), is(template.getName()));
    assertThat(returnedTemplate.getData(), is(template.getData()));
  }

  @Test
  public void shouldGetLWById() {
    Template template = createReportTemplate("Sample Report", "Consistency Report");
    TemplateParameter parameter = new TemplateParameter(template.getId(), "Parameter", "Parameter", "value", "String", "desc");
    parameter.setCreatedBy(CREATED_BY);

    mapper.insertParameter(parameter);

    Template returnedTemplate = mapper.getLWById(template.getId());

    assertThat(returnedTemplate.getId(), is(template.getId()));
    assertThat(returnedTemplate.getName(), is(template.getName()));
    assertThat(returnedTemplate.getData(), is(nullValue()));
    assertThat(returnedTemplate.getParameters().size(), is(1));
    assertTrue(returnedTemplate.getParameters().contains(parameter));
  }

  @Test
  public void shouldInsertConsistencyReportForXmlTemplateFile() {
    String type = "Consistency Report";
    String name = "Requisition expectedReportTemplate";

    Template expectedTemplate = createReportTemplate(name, type);

    TemplateParameter parameter = new TemplateParameter(expectedTemplate.getId(), "Parameter", "Parameter", "value", "String", "desc");
    mapper.insertParameter(parameter);

    Template templateDB = mapper.getById(expectedTemplate.getId());

    assertThat(templateDB.getType(), is(type));
    assertThat(templateDB.getName(), is(name));
    assertThat(templateDB.getData(), is(DATA));
    assertThat(templateDB.getCreatedBy(), is(CREATED_BY));
    assertThat(templateDB.getDescription(), is(DESCRIPTION));
    assertThat(templateDB.getParameters().size(), is(1));
    assertThat(templateDB.getParameters().get(0), is(parameter));
  }

  @Test
  public void shouldGetAllReportTemplatesAccordingToCreatedDate() {
    Template template1 = createReportTemplate("report1", "Consistency Report");
    createReportTemplate("report2", "Print");

    List<Template> templates = mapper.getAllConsistencyReportTemplates();

    assertThat(templates.size(), is(8));
    assertThat(templates.get(0).getName(), is("Facilities Missing Supporting Requisition Group"));
    assertThat(templates.get(7).getName(), is("report1"));
    assertThat(templates.get(7).getId(), is(template1.getId()));
  }

  @Test
  public void shouldGetByName() {
    Template template = createReportTemplate("Sample Report", "Consistency Report");

    Template returnedTemplate = mapper.getByName("sample report");

    assertThat(returnedTemplate.getName(), is("Sample Report"));
    assertThat(returnedTemplate.getData(), is(template.getData()));
  }

  @Test
  public void shouldInsertTemplateParameters() throws Exception {
    String type = "REPORTING";
    String name = "Requisition Report Template";
    Template template = createReportTemplate(name, type);

    TemplateParameter templateParameter = new TemplateParameter(template.getId(), "Parameter", "Parameter", "value", "String", "desc");
    templateParameter.setCreatedBy(CREATED_BY);

    mapper.insertParameter(templateParameter);

    ResultSet resultSet = queryExecutor.execute("SELECT * FROM template_parameters where templateId=?", template.getId());
    resultSet.next();

    assertThat(resultSet.getString("name"), is("Parameter"));
    assertThat(resultSet.getString("displayName"), is("Parameter"));
    assertThat(resultSet.getString("description"), is("desc"));
    assertThat(resultSet.getString("defaultValue"), is("value"));
    assertThat(resultSet.getString("dataType"), is("String"));
    assertThat(resultSet.getLong("createdBy"), is(1L));
  }

  @Test
  public void shouldGetAllTemplatesForUserOrderedByName() throws SQLException {
    Long userId = 1L;
    Template reportTemplate1 = createReportTemplate("DFacility Template1", RightType.REPORTING.toString());
    Template reportTemplate2 = createReportTemplate("AFacility Template2", RightType.REPORTING.toString());
    Template reportTemplate3 = createReportTemplate("CFacility Template3", RightType.REPORTING.toString());
    Template reportTemplate4 = createReportTemplate("EFacility Template4", RightType.REPORTING.toString());

    Right right1 = new Right(reportTemplate1.getName(), RightType.REPORTING);
    Right right2 = new Right(reportTemplate2.getName(), RightType.REPORTING);
    Right right3 = new Right(reportTemplate3.getName(), RightType.REPORTING);
    Right right4 = new Right(reportTemplate4.getName(), RightType.REPORTING);

    rightMapper.insertRight(right1);
    rightMapper.insertRight(right2);
    rightMapper.insertRight(right3);
    rightMapper.insertRight(right4);

    reportRightMapper.insert(reportTemplate1);
    reportRightMapper.insert(reportTemplate2);
    reportRightMapper.insert(reportTemplate3);
    reportRightMapper.insert(reportTemplate4);

    Role role = new Role();
    role.setName("Reporting User Role");
    roleRightsMapper.insertRole(role);
    queryExecutor.executeUpdate("INSERT INTO role_rights(roleId, rightName) VALUES (?,?)", role.getId(), reportTemplate1.getName());
    queryExecutor.executeUpdate("INSERT INTO role_rights(roleId, rightName) VALUES (?,?)", role.getId(), reportTemplate2.getName());
    queryExecutor.executeUpdate("INSERT INTO role_rights(roleId, rightName) VALUES (?,?)", role.getId(), reportTemplate3.getName());

    queryExecutor.executeUpdate("INSERT INTO role_assignments(userId, roleId) VALUES (?,?)", userId, role.getId());

    List<Template> templateList = mapper.getAllTemplatesForUser(userId);

    assertThat(templateList.size(), is(3));
    assertThat(templateList.get(0).getName(), is(reportTemplate2.getName()));
    assertThat(templateList.get(1).getName(), is(reportTemplate3.getName()));
    assertThat(templateList.get(2).getName(), is(reportTemplate1.getName()));
  }

  @Test
  public void shouldGetAllParametersByTemplateId() throws Exception {
    String type = "REPORTING";
    String name = "Requisition Report Template";
    Template template = createReportTemplate(name, type);

    TemplateParameter parameter1 = new TemplateParameter(template.getId(), "Parameter1", "Parameter1", "value", "String", "desc");
    TemplateParameter parameter2 = new TemplateParameter(template.getId(), "Parameter2", "Parameter2", "value", "String", "desc");
    mapper.insertParameter(parameter1);
    mapper.insertParameter(parameter2);

    List<TemplateParameter> parametersByTemplateId = mapper.getParametersByTemplateId(template.getId());

    assertThat(parametersByTemplateId.size(), is(2));
    assertTrue(parametersByTemplateId.contains(parameter1));
    assertTrue(parametersByTemplateId.contains(parameter2));
  }

  private Template createReportTemplate(String name, String type) {
    Template template = new Template(name, DATA, null, type, DESCRIPTION);
    template.setCreatedBy(CREATED_BY);

    mapper.insert(template);

    return template;
  }
}
