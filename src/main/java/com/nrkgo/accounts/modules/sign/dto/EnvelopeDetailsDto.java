package com.nrkgo.accounts.modules.sign.dto;

import com.nrkgo.accounts.modules.sign.model.SignDocument;
import com.nrkgo.accounts.modules.sign.model.SignEnvelope;
import com.nrkgo.accounts.modules.sign.model.SignField;
import com.nrkgo.accounts.modules.sign.model.SignRecipient;
import lombok.Data;
import java.util.List;

@Data
public class EnvelopeDetailsDto {
    private SignEnvelope envelope;
    private List<SignDocument> documents;
    private List<SignRecipient> recipients;
    private List<SignField> fields;
}
