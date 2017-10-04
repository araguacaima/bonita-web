/**
 * Copyright (C) 2009 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.forms.server.api.impl;

import static org.bonitasoft.test.toolkit.bpm.ProcessVariable.aStringVariable;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.forms.client.model.Expression;
import org.bonitasoft.forms.client.model.FormFieldValue;
import org.bonitasoft.forms.server.FormsTestCase;
import org.bonitasoft.forms.server.WaitUntil;
import org.bonitasoft.forms.server.api.FormAPIFactory;
import org.bonitasoft.forms.server.api.IFormExpressionsAPI;
import org.bonitasoft.test.toolkit.bpm.ProcessVariable;
import org.bonitasoft.test.toolkit.bpm.TestHumanTask;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for the implementation of the form expressions API
 *
 * @author Anthony Birembaut
 */
public class FormExpressionsAPIImplIT extends FormsTestCase {

    private ProcessAPI processAPI = null;

    private ProcessDefinition processDefinition = null;

    private ProcessInstance processInstance = null;

    private Expression expression = null;

    private IFormExpressionsAPI formExpressionsAPI;

    private Map<String, FormFieldValue> createFieldValueForVariable(final ProcessVariable variable, final String value) {
        final Map<String, FormFieldValue> fieldValues = new HashMap<String, FormFieldValue>();
        fieldValues.put(variable.getName(), new FormFieldValue(value, variable.getClassName()));
        return fieldValues;
    }

    @Before
    public void deployProcess() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
        processBuilder.addData(
                "application",
                String.class.getName(),
                expressionBuilder.createNewInstance("word").setContent("Word").setExpressionType(ExpressionType.TYPE_CONSTANT)
                        .setReturnType(String.class.getName()).done());
        processBuilder.addData(
                "dataWithNoInitialValue",
                String.class.getName(), null);
        processBuilder.addActor("myActor");
        processBuilder.addUserTask("Request", "myActor");
        processBuilder.addUserTask("Approval", "myActor");
        processBuilder.addTransition("Request", "Approval");

        final DesignProcessDefinition designProcessDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        final BusinessArchive businessArchive = businessArchiveBuilder
                .setFormMappings(TestProcess.createDefaultProcessFormMapping(designProcessDefinition))
                .setProcessDefinition(designProcessDefinition).done();
        processAPI = TenantAPIAccessor.getProcessAPI(getSession());
        processDefinition = processAPI.deploy(businessArchive);

        final ActorInstance processActor = processAPI.getActors(processDefinition.getId(), 0, 1, ActorCriterion.NAME_ASC).get(0);
        processAPI.addUserToActor(processActor.getId(), getInitiator().getUser().getId());

        processAPI.enableProcess(processDefinition.getId());

        processAPI = TenantAPIAccessor.getProcessAPI(getSession());
        processInstance = processAPI.startProcess(processDefinition.getId());

        final List<Expression> dependencies = new ArrayList<Expression>();
        dependencies.add(new Expression("application", "application", ExpressionType.TYPE_VARIABLE.name(), String.class.getName(), null, null));
        dependencies.add(new Expression("field_application", "field_application", ExpressionType.TYPE_INPUT.name(), String.class.getName(), null, null));
        expression = new Expression(null, "application + \"-\" + field_application", ExpressionType.TYPE_READ_ONLY_SCRIPT.name(), String.class.getName(),
                "GROOVY", dependencies);

        formExpressionsAPI = FormAPIFactory.getFormExpressionsAPI();

    }

    // FIXME
    // Can't understand this test... Why Word-Excel ?
    @Test
    public void evaluate_expression_on_finished_activity_should_TODO() throws Exception {
        final ProcessVariable aVariable = aStringVariable("application", "Word");
        final TestProcess testProcess = TestProcessFactory.createProcessWithVariables("aProcess1", aVariable);
        final TestHumanTask executedTask = testProcess.addActor(getInitiator()).enable()
                .startCase().getNextHumanTask().assignTo(getInitiator()).execute();
        final Map<String, FormFieldValue> fieldValues = createFieldValueForVariable(aVariable, "Excel");

        final Serializable evaluationResult = formExpressionsAPI.evaluateActivityExpression(getSession(), executedTask.getId(), expression, fieldValues,
                Locale.ENGLISH, false);

        assertEquals("Word-Excel", evaluationResult.toString());
    }

    // FIXME
    // Can't understand this test... Why Word-Excel ?
    @Test
    public void evaluate_expression_on_activity_should_TODO() throws Exception {
        final ProcessVariable aVariable = aStringVariable("application", "Word");
        final TestProcess testProcess = TestProcessFactory.createProcessWithVariables("aProcess2", aVariable);
        final TestHumanTask notExecutedTask = testProcess.addActor(getInitiator()).enable()
                .startCase().getNextHumanTask().assignTo(getInitiator());
        final Map<String, FormFieldValue> fieldValues = createFieldValueForVariable(aVariable, "Excel");

        final Serializable evaluationResult = formExpressionsAPI.evaluateActivityExpression(getSession(), notExecutedTask.getId(), expression, fieldValues,
                Locale.ENGLISH, false);

        assertEquals("Word-Excel", evaluationResult.toString());
    }

    @Test
    public void testEvaluateExpressionOnActivityDataWithNoInitialValue() throws Exception {
        Assert.assertTrue("no pending user task instances are found", new WaitUntil(50, 3000) {

            @Override
            protected boolean check() throws Exception {
                return processAPI.getPendingHumanTaskInstances(FormExpressionsAPIImplIT.this.getSession().getUserId(), 0, 10,
                        null).size() >= 1;
            }
        }.waitUntil());
        final HumanTaskInstance humanTaskInstance = processAPI.getPendingHumanTaskInstances(getSession().getUserId(), 0, 1,
                ActivityInstanceCriterion.NAME_ASC).get(0);
        final long activityInstanceId = humanTaskInstance.getId();
        final Expression expressionDataWithNoInitialValue = new Expression(null, "dataWithNoInitialValue", ExpressionType.TYPE_VARIABLE.name(),
                String.class.getName(),
                "NONE", new ArrayList<Expression>());
        final Serializable result = formExpressionsAPI.evaluateActivityInitialExpression(getSession(), activityInstanceId, expressionDataWithNoInitialValue,
                Locale.ENGLISH, true);
        Assert.assertNull(result);
    }

    @Test
    public void testEvaluateExpressionOnProcess() throws Exception {
        final Map<String, FormFieldValue> fieldValues = new HashMap<String, FormFieldValue>();
        fieldValues.put("application", new FormFieldValue("Excel", String.class.getName()));
        final Expression expression = new Expression(null, "field_application", ExpressionType.TYPE_INPUT.name(), String.class.getName(), null, null);
        final Serializable result = formExpressionsAPI.evaluateProcessExpression(getSession(), processDefinition.getId(), expression, fieldValues,
                Locale.ENGLISH);
        Assert.assertEquals("Excel", result.toString());
    }

    @Test
    public void testEvaluateExpressionOnProcessWithTransiantData() throws Exception {
        final Expression transientDataExpression = new Expression(null, "transientData", ExpressionType.TYPE_INPUT.name(), String.class.getName(), null, null);

        final List<Expression> dependencies = new ArrayList<Expression>();
        dependencies.add(transientDataExpression);
        final Expression expressionToEvaluate = new Expression(null, "transientData", ExpressionType.TYPE_READ_ONLY_SCRIPT.name(),
                String.class.getName(),
                "GROOVY", dependencies);

        final Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put("transientData", "transientDataValue");
        final Serializable result = formExpressionsAPI.evaluateProcessInitialExpression(getSession(), processDefinition.getId(), expressionToEvaluate,
                Locale.ENGLISH,
                context);
        Assert.assertEquals("transientDataValue", result.toString());
    }

    @Test
    public void testEvaluateExpressionsOnProcessWithTransiantData() throws Exception {
        final Expression transientDataExpression = new Expression(null, "transientData", ExpressionType.TYPE_INPUT.name(), String.class.getName(), null, null);

        final List<Expression> dependencies = new ArrayList<Expression>();
        dependencies.add(transientDataExpression);
        final Expression expressionToEvaluate = new Expression("expressionToEvaluate", "transientData",
                ExpressionType.TYPE_READ_ONLY_SCRIPT.name(),
                String.class.getName(),
                "GROOVY", dependencies);

        final Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put("transientData", "transientDataValue");
        final List<Expression> expressionsToEvaluate = new ArrayList<Expression>();
        expressionsToEvaluate.add(expressionToEvaluate);
        final Map<String, Serializable> result = formExpressionsAPI.evaluateProcessInitialExpressions(getSession(), processDefinition.getId(),
                expressionsToEvaluate,
                Locale.ENGLISH, context);
        Assert.assertEquals("transientDataValue", result.get("expressionToEvaluate"));
    }

    @Test
    public void testEvaluateExpressionOnProcessWithFieldAndTransiantData() throws Exception {
        final Map<String, FormFieldValue> fieldValues = new HashMap<String, FormFieldValue>();
        fieldValues.put("application", new FormFieldValue("Excel", String.class.getName()));
        final Expression fieldExpression = new Expression(null, "field_application", ExpressionType.TYPE_INPUT.name(), String.class.getName(), null, null);
        final Expression transientDataExpression = new Expression(null, "transientData", ExpressionType.TYPE_INPUT.name(), String.class.getName(), null, null);

        final List<Expression> dependencies = new ArrayList<Expression>();
        dependencies.add(fieldExpression);
        dependencies.add(transientDataExpression);
        final Expression expressionToEvaluate = new Expression(null, "transientData + \"-\" + field_application", ExpressionType.TYPE_READ_ONLY_SCRIPT.name(),
                String.class.getName(),
                "GROOVY", dependencies);

        final Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put("transientData", "transientDataValue");
        final Serializable result = formExpressionsAPI.evaluateProcessExpression(getSession(), processDefinition.getId(), expressionToEvaluate, fieldValues,
                Locale.ENGLISH, context);
        Assert.assertEquals("transientDataValue-Excel", result.toString());
    }

    @Test
    public void testEvaluateExpressionsOnProcessWithFieldAndTransiantData() throws Exception {
        final Map<String, FormFieldValue> fieldValues = new HashMap<String, FormFieldValue>();
        fieldValues.put("application", new FormFieldValue("Excel", String.class.getName()));
        final Expression fieldExpression = new Expression(null, "field_application", ExpressionType.TYPE_INPUT.name(), String.class.getName(), null, null);
        final Expression transientDataExpression = new Expression(null, "transientData", ExpressionType.TYPE_INPUT.name(), String.class.getName(), null, null);

        final List<Expression> dependencies = new ArrayList<Expression>();
        dependencies.add(fieldExpression);
        dependencies.add(transientDataExpression);
        final Expression expressionToEvaluate = new Expression("expressionToEvaluate", "transientData + \"-\" + field_application",
                ExpressionType.TYPE_READ_ONLY_SCRIPT.name(),
                String.class.getName(),
                "GROOVY", dependencies);

        final Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put("transientData", "transientDataValue");
        final List<Expression> expressionsToEvaluate = new ArrayList<Expression>();
        expressionsToEvaluate.add(expressionToEvaluate);
        final Map<String, Serializable> result = formExpressionsAPI.evaluateProcessExpressions(getSession(), processDefinition.getId(), expressionsToEvaluate,
                fieldValues,
                Locale.ENGLISH, context);
        Assert.assertEquals("transientDataValue-Excel", result.get("expressionToEvaluate"));
    }

    @Test
    public void testEvaluateExpressionOnInstanceWithInitialValues() throws Exception {
        Assert.assertTrue("no pending user task instances are found", new WaitUntil(50, 3000) {

            @Override
            protected boolean check() throws Exception {
                return processAPI.getPendingHumanTaskInstances(FormExpressionsAPIImplIT.this.getSession().getUserId(), 0, 10,
                        null).size() >= 1;
            }
        }.waitUntil());
        final HumanTaskInstance humanTaskInstance = processAPI.getPendingHumanTaskInstances(getSession().getUserId(), 0, 1,
                ActivityInstanceCriterion.NAME_ASC).get(0);
        final long activityInstanceId = humanTaskInstance.getId();
        processAPI.assignUserTask(activityInstanceId, getSession().getUserId());
        processAPI.executeFlowNode(activityInstanceId);
        processAPI.updateProcessDataInstance("application", processInstance.getId(), "Excel");
        final List<Expression> dependencies = new ArrayList<Expression>();
        dependencies.add(new Expression("application", "application", ExpressionType.TYPE_VARIABLE.name(), String.class.getName(), null, null));
        final Expression expression = new Expression(null, "application", ExpressionType.TYPE_READ_ONLY_SCRIPT.name(), String.class.getName(), "GROOVY",
                dependencies);
        Assert.assertTrue("no pending user task instances are found", new WaitUntil(50, 3000) {

            @Override
            protected boolean check() throws Exception {
                return processAPI.getPendingHumanTaskInstances(FormExpressionsAPIImplIT.this.getSession().getUserId(), 0, 10,
                        null).size() >= 1;
            }
        }.waitUntil());
        final Serializable result = formExpressionsAPI.evaluateInstanceInitialExpression(getSession(), processInstance.getId(), expression, Locale.ENGLISH,
                false);
        Assert.assertEquals("Word", result.toString());
    }

    @Test
    public void testEvaluateExpressionOnTerminatedInstanceWithInitialValues() throws Exception {
        // Terminate process
        Assert.assertTrue("no pending user task instances are found", new WaitUntil(50, 1000) {

            @Override
            protected boolean check() throws Exception {
                return processAPI.getPendingHumanTaskInstances(FormExpressionsAPIImplIT.this.getSession().getUserId(), 0, 10,
                        null).size() >= 1;
            }
        }.waitUntil());
        HumanTaskInstance humanTaskInstance = processAPI.getPendingHumanTaskInstances(getSession().getUserId(), 0, 1,
                ActivityInstanceCriterion.NAME_ASC).get(0);
        long activityInstanceId = humanTaskInstance.getId();
        processAPI.assignUserTask(activityInstanceId, getSession().getUserId());
        processAPI.executeFlowNode(activityInstanceId);
        processAPI.updateProcessDataInstance("application", processInstance.getId(), "Excel");
        Assert.assertTrue("no pending user task instances are found", new WaitUntil(50, 3000) {

            @Override
            protected boolean check() throws Exception {
                return processAPI.getPendingHumanTaskInstances(FormExpressionsAPIImplIT.this.getSession().getUserId(), 0, 10,
                        null).size() >= 1;
            }
        }.waitUntil());
        humanTaskInstance = processAPI.getPendingHumanTaskInstances(getSession().getUserId(), 0, 1, ActivityInstanceCriterion.NAME_ASC).get(0);
        activityInstanceId = humanTaskInstance.getId();
        processAPI.assignUserTask(activityInstanceId, getSession().getUserId());
        processAPI.executeFlowNode(activityInstanceId);
        Assert.assertTrue("Process instance still not archived", new WaitUntil(50, 3000) {

            @Override
            protected boolean check() throws Exception {
                try {
                    processAPI.getProcessInstance(processInstance.getId());
                    return false;
                } catch (final ProcessInstanceNotFoundException e) {
                    return true;
                }
            }
        }.waitUntil());
        final List<Expression> dependencies = new ArrayList<Expression>();
        dependencies.add(new Expression("application", "application", ExpressionType.TYPE_VARIABLE.name(), String.class.getName(), null, null));
        final Expression expression = new Expression(null, "application", ExpressionType.TYPE_READ_ONLY_SCRIPT.name(), String.class.getName(), "GROOVY",
                dependencies);
        final ArchivedProcessInstance archivedProcessInstance = processAPI.getArchivedProcessInstances(processInstance.getId(), 0, 1).get(0);

        final Serializable result = formExpressionsAPI.evaluateInstanceInitialExpression(getSession(), archivedProcessInstance.getSourceObjectId(), expression,
                Locale.ENGLISH, false);
        Assert.assertEquals("Word", result.toString());
    }

    @Test
    public void testEvaluateExpressionOnInstanceWithCurrentValues() throws Exception {
        Assert.assertTrue("no pending user task instances are found", new WaitUntil(50, 3000) {

            @Override
            protected boolean check() throws Exception {
                return processAPI.getPendingHumanTaskInstances(FormExpressionsAPIImplIT.this.getSession().getUserId(), 0, 10,
                        null).size() >= 1;
            }
        }.waitUntil());
        final HumanTaskInstance humanTaskInstance = processAPI.getPendingHumanTaskInstances(getSession().getUserId(), 0, 1,
                ActivityInstanceCriterion.NAME_ASC).get(0);
        final long activityInstanceId = humanTaskInstance.getId();
        processAPI.assignUserTask(activityInstanceId, getSession().getUserId());
        processAPI.executeFlowNode(activityInstanceId);
        processAPI.updateProcessDataInstance("application", processInstance.getId(), "Excel");
        final List<Expression> dependencies = new ArrayList<Expression>();
        dependencies.add(new Expression("application", "application", ExpressionType.TYPE_VARIABLE.name(), String.class.getName(), null, null));
        final Expression expression = new Expression(null, "application", ExpressionType.TYPE_READ_ONLY_SCRIPT.name(), String.class.getName(), "GROOVY",
                dependencies);
        final Serializable result = formExpressionsAPI.evaluateInstanceInitialExpression(getSession(), processInstance.getId(), expression, Locale.ENGLISH,
                true);
        Assert.assertEquals("Excel", result.toString());
    }

    @Test
    public void should_getDocumentValue_work_with_existing_document_and_no_new_URL() throws Exception {

        final Document doc = processAPI.attachDocument(processInstance.getId(), "documentName", null, null, "www.bonitasoft.org");

        final FormFieldValue fieldValue = new FormFieldValue("www.bonitasoft.org", String.class.getName());
        fieldValue.setDocumentName(doc.getName());
        fieldValue.setDocument(true);
        fieldValue.setDocumentId(doc.getId());

        final FormExpressionsAPIImpl formExpressionAPIImpl = spy(new FormExpressionsAPIImpl());

        doReturn(15000L).when(formExpressionAPIImpl).getDocumentMaxSize(getSession());

        final DocumentValue result = formExpressionAPIImpl.getDocumentValue(getSession(), fieldValue, true);

        Assert.assertEquals(doc.getId(), result.getDocumentId().longValue());
        Assert.assertFalse(result.hasChanged());
    }

    @Test
    public void should_getDocumentValue_work_with_existing_document_and_new_URL() throws Exception {

        final Document doc = processAPI.attachDocument(processInstance.getId(), "documentName", null, null, "www.bonitasoft.org");

        final FormFieldValue fieldValue = new FormFieldValue("www.google.com", String.class.getName());
        fieldValue.setDocumentName(doc.getName());
        fieldValue.setDocument(true);
        fieldValue.setDocumentId(doc.getId());

        final FormExpressionsAPIImpl formExpressionAPIImpl = spy(new FormExpressionsAPIImpl());

        doReturn(15000L).when(formExpressionAPIImpl).getDocumentMaxSize(getSession());

        final DocumentValue result = formExpressionAPIImpl.getDocumentValue(getSession(), fieldValue, true);

        Assert.assertEquals(doc.getId(), result.getDocumentId().longValue());
        Assert.assertTrue(result.hasChanged());
        Assert.assertEquals("www.google.com", result.getUrl());
    }

    @Test
    public void should_getDocumentValue_work_with_existing_document_and_no_new_file() throws Exception {

        final Document doc = processAPI.attachDocument(processInstance.getId(), "documentName", "initialDoc.txt", null, new byte[] { 5, 0, 1, 4, 6, 5, 2, 3, 1,
                5, 6, 8, 4, 6, 6, 3, 2, 4, 5 });

        final FormFieldValue fieldValue = new FormFieldValue();
        fieldValue.setValueType(File.class.getName());
        fieldValue.setDocumentName(doc.getName());
        fieldValue.setDocument(true);
        fieldValue.setDocumentId(doc.getId());
        fieldValue.setDisplayedValue(doc.getContentFileName());

        final FormExpressionsAPIImpl formExpressionAPIImpl = spy(new FormExpressionsAPIImpl());

        doReturn(15000L).when(formExpressionAPIImpl).getDocumentMaxSize(getSession());

        final DocumentValue result = formExpressionAPIImpl.getDocumentValue(getSession(), fieldValue, true);

        Assert.assertEquals(doc.getId(), result.getDocumentId().longValue());
        Assert.assertFalse(result.hasChanged());
    }

    @Test
    public void should_getDocumentValue_work_with_existing_document_and_new_file() throws Exception {

        final Document doc = processAPI.attachDocument(processInstance.getId(), "documentName", "initialDoc.txt", null, new byte[] { 5, 0, 1, 4, 6, 5, 2, 3, 1,
                5, 6, 8, 4, 6, 6, 3, 2, 4, 5 });

        final File file = File.createTempFile("testDoc", "txt");
        file.deleteOnExit();
        FileUtils.writeStringToFile(file, "new content");
        final FormFieldValue fieldValue = new FormFieldValue(file.getPath(), File.class.getName());
        fieldValue.setDocumentName(doc.getName());
        fieldValue.setDocument(true);
        fieldValue.setDocumentId(doc.getId());
        fieldValue.setDisplayedValue(file.getName());

        final FormExpressionsAPIImpl formExpressionAPIImpl = spy(new FormExpressionsAPIImpl());
        final BonitaHomeFolderAccessor tenantFolder = mock(BonitaHomeFolderAccessor.class);
        doReturn(tenantFolder).when(formExpressionAPIImpl).getTenantFolder();
        doReturn(file).when(tenantFolder).getTempFile(any(String.class), any(Long.class));

        doReturn(15000L).when(formExpressionAPIImpl).getDocumentMaxSize(getSession());

        final DocumentValue result = formExpressionAPIImpl.getDocumentValue(getSession(), fieldValue, true);

        Assert.assertEquals(doc.getId(), result.getDocumentId().longValue());
        Assert.assertTrue(result.hasChanged());
        Assert.assertEquals(file.getName(), result.getFileName());
    }

    @After
    public void tearDown() throws Exception {
        processAPI.disableProcess(processDefinition.getId());
        do {
        } while (processAPI.deleteProcessInstances(processDefinition.getId(), 0, 20) > 0);
        do {
        } while (processAPI.deleteArchivedProcessInstances(processDefinition.getId(), 0, 20) > 0);
        processAPI.deleteProcessDefinition(processDefinition.getId());

    }

}
