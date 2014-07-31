package controllers;

import static play.data.Form.form;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import models.Doctor;
import models.Patient;
import models.Visit;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.visit;
import views.html.visits;
import controllers.pagination.CriteriaApplier;
import controllers.pagination.CriteriaPaginator;

@Security.Authenticated(Secured.class)
public class VisitController extends Controller {
	
	@Transactional
	public static Result create(long patientId) {
		System.out.println("patientId: " + patientId);
		Visit visit = new Visit();
		Patient patient = JPA.em().find(Patient.class, patientId);
		System.out.println(patient);
		visit.setPatient(patient);
		JPA.em().persist(visit);
		return get(visit.getId());
	}
	
	@Transactional
	public static Result get(long id) {
		Visit visitEntity = JPA.em().find(Visit.class, id);
		Form<Visit> form = form(Visit.class);
		form = form.fill(visitEntity);
		return ok(visit.render(form));
	}
	
	//TODO: return all if page=0 (for mobile client which won't have pagination)
	@Transactional
	public static Result listAASFSAF(int page, final boolean onlyMine, final String fromDate, final String toDate, final String patientId) {
		return list(page, onlyMine, fromDate, toDate, null);
	}
	
	//TODO: return all if page=0 (for mobile client which won't have pagination)
	@Transactional
	public static Result list(int page, final boolean onlyMine, final String fromDate, final String toDate, final Long patientId) {
		Patient patient = null;
		if (patientId != -1) {
			patient = JPA.em().find(Patient.class, patientId);
		}
		final Patient finalPatient = patient;
		CriteriaApplier conditionsApplier = new CriteriaApplier() {
			@Override
			public <S, T> void applyCondition(CriteriaBuilder cb,
					CriteriaQuery<S> criteriaQuery, Root<T> root) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (onlyMine) {
					predicates.add(cb.equal(root.get("doctor"), Application.getLoggedInDoctor()));
				}
				if (toDate != null && !toDate.isEmpty()) {
					try {
						predicates.add(cb.lessThanOrEqualTo(root.<Date>get("date"), Application.dateFormatter.parse(toDate)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				if (fromDate != null && !fromDate.isEmpty()) {
					try {
						predicates.add(cb.greaterThanOrEqualTo(root.<Date>get("date"), Application.dateFormatter.parse(fromDate)));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				if (finalPatient != null) {
					predicates.add(cb.equal(root.get("patient"), finalPatient));
				}
				if (predicates.size() > 0) {
					criteriaQuery.where(predicates.toArray(new Predicate[0]));
				}
				
			}
			@Override
			public <S, T> void applyOrder(CriteriaBuilder builder,
					CriteriaQuery<S> criteriaQuery, Root<T> root) {
				criteriaQuery.orderBy(builder.asc(root.get("date")));
			}
		};
		CriteriaPaginator<Visit> paginator = new CriteriaPaginator<Visit>(Visit.class, conditionsApplier);
		return ok(visits.render(page, paginator.get(page), paginator.getPageCount(), onlyMine, fromDate, toDate, finalPatient));
	}
	
	@Transactional
	public static Result save() {
		Form<Visit> visitForm = Form.form(Visit.class);
		Visit visit = visitForm.bindFromRequest().get();
		Visit oldVisit = JPA.em().find(Visit.class, visit.getId());
		visit.setPatient(oldVisit.getPatient());
		Doctor doctor = Application.getLoggedInDoctor();
		visit.setDoctor(doctor);
		JPA.em().merge(visit);
		return defaultList();
	}
	
	@Transactional
	public static Result remove(long id) {
		Visit visit = JPA.em().find(Visit.class, id);
		JPA.em().remove(visit);
		return defaultList();
	}
	
	public static Result defaultList() {
		return list(1, false, null, null, -1L);
	}

}
