package com.evolveum.midpoint.web.page.login;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.evolveum.midpoint.web.component.form.Form;
import com.evolveum.midpoint.common.policy.ValuePolicyGenerator;
import com.evolveum.midpoint.gui.api.component.autocomplete.AutoCompleteTextPanel;
import com.evolveum.midpoint.gui.api.component.password.PasswordPanel;
import com.evolveum.midpoint.gui.api.model.LoadableModel;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.gui.api.util.WebModelServiceUtils;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismObjectDefinition;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.builder.QueryBuilder;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.result.OperationResultStatus;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.Producer;
import com.evolveum.midpoint.util.exception.CommunicationException;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.web.application.PageDescriptor;
import com.evolveum.midpoint.web.component.AjaxSubmitButton;
import com.evolveum.midpoint.web.component.input.TextPanel;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.web.page.admin.configuration.component.EmptyOnBlurAjaxFormUpdatingBehaviour;
import com.evolveum.midpoint.xml.ns._public.common.common_3.CredentialsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OrgType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.PasswordType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SystemConfigurationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SystemObjectsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ValuePolicyType;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import com.evolveum.prism.xml.ns._public.types_3.ProtectedStringType;


//"http://localhost:8080/midpoint/confirm/registrationid=" + newUser.getOid()
//+ "/token=" + userType.getCostCenter() + "/roleId=00000000-0000-0000-0000-000000000008";
@PageDescriptor(url = "/registration")
public class PageSelfRegistration extends PageBase {
	
	private static final String DOT_CLASS  = PageSelfRegistration.class.getName() + ".";

	private static final String ID_MAIN_FORM = "mainForm";
	private static final String ID_FIRST_NAME = "firstName";
	private static final String ID_LAST_NAME = "lastName";
	private static final String ID_EMAIL = "email";
	private static final String ID_ORGANIZATION = "organization";
	private static final String ID_PASSWORD = "password";
	private static final String ID_SUBMIT_REGISTRATION = "submitRegistration";
	private static final String ID_REGISTRATION_SUBMITED = "registrationInfo";

	private static final String OPERATION_SAVE_USER = DOT_CLASS + "saveUser";
	private static final String OPERATION_LOAD_ORGANIZATIONS = DOT_CLASS + "loadOrganization";

	private static final long serialVersionUID = 1L;

	IModel<UserType> userModel;
	boolean submited = false;

	public PageSelfRegistration() {

		final UserType user = createUser();

		userModel = new LoadableModel<UserType>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected UserType load() {
				return user;
			}
		};

		initLayout();
	}

	private UserType createUser() {
		PrismObjectDefinition<UserType> userDef = getPrismContext().getSchemaRegistry()
				.findObjectDefinitionByCompileTimeClass(UserType.class);
		PrismObject<UserType> user;
		try {
			user = userDef.instantiate();
		} catch (SchemaException e) {
			UserType userType = new UserType();
			user = userType.asPrismObject();

		}

		return user.asObjectable();
	}

	private void initLayout() {
		Form<?> mainForm = new Form<>(ID_MAIN_FORM);
		mainForm.add(new VisibleEnableBehaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return !submited;
			}

			@Override
			public boolean isEnabled() {
				return !submited;
			}
		});
		add(mainForm);

		TextPanel<String> firstName = new TextPanel<>(ID_FIRST_NAME,
				new PropertyModel<String>(userModel, UserType.F_GIVEN_NAME.getLocalPart() + ".orig") {

					@Override
					public void setObject(String object) {
						userModel.getObject().setGivenName(new PolyStringType(object));
					}
				});
		firstName.getBaseFormComponent().add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
		mainForm.add(firstName);

		TextPanel<String> lastName = new TextPanel<>(ID_LAST_NAME,
				new PropertyModel<String>(userModel, UserType.F_FAMILY_NAME.getLocalPart() + ".orig") {

					@Override
					public void setObject(String object) {
						userModel.getObject().setFamilyName(new PolyStringType(object));
					}

				});
		lastName.getBaseFormComponent().add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
		mainForm.add(lastName);

		TextPanel<String> email = new TextPanel<>(ID_EMAIL,
				new PropertyModel<String>(userModel, UserType.F_EMAIL_ADDRESS.getLocalPart()));
		email.getBaseFormComponent().add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
		mainForm.add(email);

		AutoCompleteTextPanel<String> organization = new AutoCompleteTextPanel<String>(ID_ORGANIZATION,
				Model.of(""), String.class) {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<String> getIterator(String input) {
				return prepareAutocompleteValues(input).iterator();
			}

		};
		// organization.getBaseFormComponent().add(new
		// EmptyOnBlurAjaxFormUpdatingBehaviour());
		mainForm.add(organization);

		PasswordPanel password = new PasswordPanel(ID_PASSWORD,
				new PropertyModel<ProtectedStringType>(userModel,
						UserType.F_CREDENTIALS.getLocalPart() + "."
								+ CredentialsType.F_PASSWORD.getLocalPart() + "."
								+ PasswordType.F_VALUE.getLocalPart()));
		password.getBaseFormComponent().add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
		mainForm.add(password);

		AjaxSubmitButton register = new AjaxSubmitButton(ID_SUBMIT_REGISTRATION) {

			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target,
					org.apache.wicket.markup.html.form.Form<?> form) {
				saveUser(target);
			}

		};
		mainForm.add(register);

		MultiLineLabel label = new MultiLineLabel(ID_REGISTRATION_SUBMITED, createStringResource("PageSelfRegistration.registration.confirm.message"));
		add(label);
		label.add(new VisibleEnableBehaviour() {

			@Override
			public boolean isVisible() {
				return submited;
			}

			@Override
			public boolean isEnabled() {
				return submited;
			}

		});

	}

	private List<String> prepareAutocompleteValues(final String input) {

		return runPrivileged(new Producer<List<String>>() {
			@Override
			public List<String> run() {
				Collection<PrismObject<OrgType>> objects;
				int maxValues = 15;
				List<String> availableNames = new ArrayList<>();
				try {
					ObjectQuery query = QueryBuilder.queryFor(OrgType.class, getPrismContext())
							.item(OrgType.F_NAME).startsWith(input).build();
					query.setPaging(ObjectPaging.createPaging(0, maxValues));
					Task task = createAnonymousTask(OPERATION_LOAD_ORGANIZATIONS);
					OperationResult result = new OperationResult(OPERATION_LOAD_ORGANIZATIONS);
					objects = getModelService().searchObjects(OrgType.class, query, null, task, result);

					for (PrismObject<OrgType> o : objects) {
						String objectName = WebComponentUtil.getDisplayNameOrName(o);
						availableNames.add(objectName);
					}

				} catch (SchemaException | ObjectNotFoundException | SecurityViolationException
						| CommunicationException | ConfigurationException e) {
					error("Failed to prepare autocomplete field");
				}
				return availableNames;
			}

		});

	}

	private void saveUser(AjaxRequestTarget target) {
		OperationResult result = runPrivileged(new Producer<OperationResult>() {

			@Override
			public OperationResult run() {
				String organization = getOrganization();
				UserType userType = userModel.getObject();
				if (organization != null) {
					userType.getOrganization().add(new PolyStringType(organization));
				}
				
				Task task = createAnonymousTask(OPERATION_SAVE_USER);
				task.setChannel(SchemaConstants.CHANNEL_GUI_REGISTRATION_URI);
				OperationResult result = new OperationResult(OPERATION_SAVE_USER);
				
				PrismObject<SystemConfigurationType> systemConfig = WebModelServiceUtils.loadObject(SystemConfigurationType.class, SystemObjectsType.SYSTEM_CONFIGURATION.value(), PageSelfRegistration.this, task, result);
				
				String token = null;
				ValuePolicyType policy = null;
				if (systemConfig.asObjectable().getGlobalPasswordPolicyRef()  != null) {
					PrismObject<ValuePolicyType> valuePolicy = WebModelServiceUtils.loadObject(ValuePolicyType.class, systemConfig.asObjectable().getGlobalPasswordPolicyRef().getOid(), PageSelfRegistration.this, task, result);
					policy = valuePolicy.asObjectable();
				}
				
				token = ValuePolicyGenerator.generate(policy != null ? policy.getStringPolicy() : null, 24, result);
				userType.setCostCenter(token);
				
				ObjectDelta<UserType> userDelta= ObjectDelta.createAddDelta(userType.asPrismObject());
				userDelta.setPrismContext(getPrismContext());
				
				WebModelServiceUtils.save(userDelta, result, task, PageSelfRegistration.this);
				return result;
			}
			
		});
		
		result.computeStatus();
		
		if (result.getStatus() == OperationResultStatus.SUCCESS) {
			submited = true;
			success(createStringResource("PageSelfRegistration.registration.success").getString());
			
		} else {
			error(createStringResource("PageSelfRegistration.registration.error", result.getMessage()).getString());
		}
		target.add(getFeedbackPanel());
		target.add(this);
		
	}

	private String getOrganization() {
		AutoCompleteTextPanel<String> org = (AutoCompleteTextPanel<String>) get(
				createComponentPath(ID_MAIN_FORM, ID_ORGANIZATION));
		return org.getBaseFormComponent().getModel().getObject();
	}

}