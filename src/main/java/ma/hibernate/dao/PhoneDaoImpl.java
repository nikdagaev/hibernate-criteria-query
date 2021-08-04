package ma.hibernate.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = factory.openSession();
            transaction = session.beginTransaction();
            session.save(phone);
            transaction.commit();
            return phone;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Can't create a phone " + phone, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Phone> findAllQuery = criteriaBuilder.createQuery(Phone.class);
            Root<Phone> phoneRoot = findAllQuery.from(Phone.class);
            Predicate[] predicates = new Predicate[params.size()];
            int counter = 0;
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                CriteriaBuilder.In<String> parameterPredicate
                        = criteriaBuilder.in(phoneRoot.get(entry.getKey()));
                for (String s : entry.getValue()) {
                    parameterPredicate.value(s);
                }
                predicates[counter] = parameterPredicate;
                counter++;
            }
            findAllQuery.where(criteriaBuilder.and(predicates));
            return session.createQuery(findAllQuery).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error while fining a phone by the following parameters: "
                    + params.entrySet().stream()
                    .map(m -> m.getKey() + ": " + Arrays.toString(m.getValue()))
                    .collect(Collectors.joining(", ")), e);
        }
    }
}
