package com.je.UserService.projection;

import com.je.CommonService.model.CardDetails;
import com.je.CommonService.model.User;
import com.je.CommonService.queries.GetUserPaymentDetailsQuery;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserProjection {

    @QueryHandler
    public User getUserPaymentDetails(GetUserPaymentDetailsQuery query) {
        //Ideally Get the details from the DB
        CardDetails cardDetails
                = CardDetails.builder()
                .name("Shabbir Dawoodi")
                .validUntilYear(2022)
                .validUntilMonth(01)
                .cardNumber("123456789")
                .cvv(111)
                .build();

        return User.builder()
                .userId(query.getUserId())
                .firstName("Shabbir")
                .lastName("Dawoodi")
                .cardDetails(cardDetails)
                .build();
    }
}
