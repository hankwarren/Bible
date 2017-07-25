package com.kgdsoftware.bible.model;

// Ussage:
//  Query version status - if complete use local table
//      if not complete, query the version status table
//      if 
// create version status table
// query status of a verseId
// insert status record - GETTING
// update status record - GOTTEN
/**
 *
 * @author henriwarren
 */
public class VersionStatus {
    private int mId;
    private int mVerseId;
    private int mVersionId;
    private int mStatus;
    private int mResult;
    
    public VersionStatus() {
        
    }
}
