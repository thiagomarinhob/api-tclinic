ALTER TABLE patients_aud         RENAME COLUMN rev_type TO revtype;
ALTER TABLE medical_records_aud  RENAME COLUMN rev_type TO revtype;
ALTER TABLE appointments_aud     RENAME COLUMN rev_type TO revtype;
ALTER TABLE patient_consents_aud RENAME COLUMN rev_type TO revtype;
ALTER TABLE professionals_aud    RENAME COLUMN rev_type TO revtype;
