package com.sforce.soap.partner;

/**
 * Generated by ComplexTypeCodeGenerator.java. Please do not edit.
 */
public interface IGetDeletedResult  {

      /**
       * element : deletedRecords of type {urn:partner.soap.sforce.com}DeletedRecord
       * java type: com.sforce.soap.partner.DeletedRecord[]
       */

      public com.sforce.soap.partner.IDeletedRecord[] getDeletedRecords();

      public void setDeletedRecords(com.sforce.soap.partner.IDeletedRecord[] deletedRecords);

      /**
       * element : earliestDateAvailable of type {http://www.w3.org/2001/XMLSchema}dateTime
       * java type: java.util.Calendar
       */

      public java.util.Calendar getEarliestDateAvailable();

      public void setEarliestDateAvailable(java.util.Calendar earliestDateAvailable);

      /**
       * element : latestDateCovered of type {http://www.w3.org/2001/XMLSchema}dateTime
       * java type: java.util.Calendar
       */

      public java.util.Calendar getLatestDateCovered();

      public void setLatestDateCovered(java.util.Calendar latestDateCovered);


}