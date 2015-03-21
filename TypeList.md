The page contains a listing of all the vCard properties ez-vcard supports, and also includes code samples.

# ADR #

Defines a mailing address.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.11](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.11](http://tools.ietf.org/html/rfc2426#page-11) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.32](http://tools.ietf.org/html/rfc6350#page-32) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Address](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Address.html)

```
VCard vcard = new VCard();

Address adr = new Address();
adr.setStreetAddress("123 Main St.");
adr.setLocality("Austin");
adr.setRegion("TX");
adr.setPostalCode("12345");
adr.setCountry("USA");
adr.addType(AddressType.WORK);

//optionally, set the mailing label text
adr.setLabel("123 Main St.\nAustin, TX 12345\nUSA");

vcard.addAddress(adr);
```

```
VCard vcard = ...
for (Address adr : vcard.getAddresses()){
  String street = adr.getStreetAddress();
  String city = adr.getLocality();
  //etc.
}
```

# AGENT #
Defines information about the person's agent.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.18](http://www.imc.org/pdi/vcard-21.doc) |![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.19](http://tools.ietf.org/html/rfc2426#page-19) | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **4.0** |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Agent](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Agent.html)

```
VCard vcard = new VCard();

//URL
Agent agent = new Agent("http://www.linkedin.com/BobSmith");
vcard.setAgent(agent);

//vCard
VCard agentVCard = new VCard();
agentVCard.setFormattedName("Bob Smith");
agentVCard.addTelephoneNumber("(555) 123-4566");
agentVCard.addUrl("http://www.linkedin.com/BobSmith");
agent = new Agent(agentVCard);
vcard.setAgent(agent);
```

```
VCard vcard = ...
Agent agent = vcard.getAgent();

String url = agent.getUrl();
if (url != null){
  //property value is a URL
}

VCard agentVCard = agent.getVCard();
if (agentVCard != null){
  //property value is a vCard
}
```

# ANNIVERSARY #
Defines the person's anniversary.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** |![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.31](http://tools.ietf.org/html/rfc6350#page-31) |
|:------------------------------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Anniversary](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Anniversary.html)

```
VCard vcard = new VCard();

//date
Calendar c = Calendar.getInstance();
c.clear();
c.set(Calendar.YEAR, 1970);
c.set(Calendar.MONTH, Calendar.MARCH);
c.set(Calendar.DAY_OF_MONTH, 21);
Anniversary anniversary = new Anniversary(c.getTime());
vcard.setAnniversary(anniversary);

//partial date (e.g. just the month and date)
PartialDate date = PartialDate.date(null, 3, 21);
anniversary = new Anniversary(date); //March 21
vcard.setAnniversary(anniversary);

//plain text value
anniversary = new Anniversary("Over 20 years ago!");
vcard.setAnniversary(anniversary);
```

```
VCard vcard = ...
Anniversary anniversary = vcard.getAnniversary();

Date date = anniversary.getDate();
if (date != null){
  //property value is a date
}

PartialDate partialDate = anniversary.getPartialDate();
if (partialDate != null){
  //property value is a partial date
  int year = partialDate.getYear();
  int month = partialDate.getMonth();
}

String text = anniversary.getText();
if (text != null){
  //property value is plain text
}
```

# BDAY #
Defines the person's birthday.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.11](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.11](http://tools.ietf.org/html/rfc2426#page-11) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.30](http://tools.ietf.org/html/rfc6350#page-30) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Birthday](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Birthday.html)

```
VCard vcard = new VCard();

//date
Calendar c = Calendar.getInstance();
c.clear();
c.set(Calendar.YEAR, 1912);
c.set(Calendar.MONTH, Calendar.JUNE);
c.set(Calendar.DAY_OF_MONTH, 23);
Birthday bday = new Birthday(c.getTime());
vcard.setBirthday(bday);

//partial date (e.g. just the month and date, vCard 4.0 only)
bday = new Birthday(PartialDate.date(null, 6, 23)); //June 23
vcard.setBirthday(bday);

//plain text value (vCard 4.0 only)
bday = new Birthday("Don't even go there, dude...");
vcard.setBirthday(bday);
```

```
VCard vcard = ...
Birthday bday = vcard.getBirthday();

Date date = bday.getDate();
if (date != null){
  //property value is a date
}

PartialDate partialDate = bday.getPartialDate();
if (partialDate != null){
  //property value is a partial date
  int year = partialDate.getYear();
  int month = partialDate.getMonth();
}

String text = bday.getText();
if (text != null){
  //property value is plain text
}
```

# BIRTHPLACE #
Defines the location of the person's birth.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/question.png](https://ez-vcard.googlecode.com/svn/wiki/images/question.png) **4.0**`*` [RFC 6474 p.2](http://tools.ietf.org/html/rfc6474#page-2) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

`*` Defined in separate specification.

**Java class:** [ezvcard.property.Birthplace](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Birthplace.html)

```
VCard vcard = new VCard();

//text
Birthplace birthplace = new Birthplace("Maida Vale, London, United Kingdom");
vcard.setBirthplace(birthplace);

//geo coordinates
birthplace = new Birthplace(51.5274, -0.1899);
vcard.setBirthplace(birthplace);

//URI
birthplace = new Birthplace();
birthplace.setUri("http://en.wikipedia.org/wiki/Maida_Vale");
vcard.setBirthplace(birthplace);
```

```
VCard vcard = ...
Birthplace birthplace = vcard.getBirthplace();

String text = birthplace.getText();
if (text != null){
  //property value is plain text
}

Double latitude = birthplace.getLatitude();
Double longitude = birthplace.getLongitude();
if (latitude != null){
  //property value is a set of geo coordinates
}

String uri = birthplace.getUri();
if (uri != null){
  //property value is a URI
}
```

# CALADRURI #
Defines a URL to use for sending a scheduling request to the person's calendar.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.50](http://tools.ietf.org/html/rfc6350#page-50) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.CalendarRequestUri](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/CalendarRequestUri.html)

```
VCard vcard = new VCard();
vcard.addCalendarRequestUri("mailto:janedoe@ibm.com");
```

# CALURI #
Defines a URL that points to the person's calendar.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.50](http://tools.ietf.org/html/rfc6350#page-50) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.CalendarUri](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/CalendarUri.html)

```
VCard vcard = new VCard();
vcard.addCalendarUri("http://www.ibm.com/janedoe/calendar");
```

# CATEGORIES #
Defines a list of keywords that can be used to describe the person.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.20](http://tools.ietf.org/html/rfc2426#page-20) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.43](http://tools.ietf.org/html/rfc6350#page-43) |
|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Categories](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Categories.html)

```
VCard vcard = new VCard();
vcard.setCategories("Developer", "Java coder", "Ladies' man");
```

# CLASS #
Describes the sensitivity of the information in the vCard.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.26](http://tools.ietf.org/html/rfc2426#page-26) | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **4.0** |
|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Classification](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Classification.html)

```
VCard vcard = new VCard();
//sample values: PUBLIC, PRIVATE, CONFIDENTIAL
vcard.setClassification("PUBLIC");
```

# CLIENTPIDMAP #
Maps a globally-unique URI to a PID parameter value.  The PID parameter can be set on any property where multiple instances are allowed (such as EMAIL or ADR, but not N because only 1 instance of N is allowed).  It allows an individual property instance to be uniquely identifiable.

The CLIENTPIDMAP property and the PID parameter are used during the synchronization (merging) process of two versions of the same vCard.  For example, if the user has a copy of her vCard on her desktop computer and her smart phone, and she makes different modifications to each copy, then the two copies could be synchronized in order to merge all the changes into a single, new vCard.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.47](http://tools.ietf.org/html/rfc6350#page-47) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.ClientPidMap](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/ClientPidMap.html)

```
VCard vcard = new VCard();

Address adr = new Address();
adr.addPid(1, 1);
vcard.addAddress(adr);

Email email = vcard.addEmail("johndoe@hotmail.com");
emai.addPid(1, 1);
email = vcard.addEmail("jdoe@company.com");
email.addPid(2, 2);

//specify the URI to use
ClientPidMap clientpidmap = new ClientPidMap(1, "urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af");
vcard.addClientPidMap(clientpidmap);

//or, generate a random URI
clientpidmap = ClientPidMap.random(2);
vcard.addClientPidMap(clientpidmap);
```

# DEATHDATE #
Defines the person's time of death.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/question.png](https://ez-vcard.googlecode.com/svn/wiki/images/question.png) **4.0**`*` [RFC 6474 p.4](http://tools.ietf.org/html/rfc6474#page-4) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

`*` Defined in separate specification.

**Java class:** [ezvcard.property.Deathdate](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Deathdate.html)

```
VCard vcard = new VCard();

//date
Calendar c = Calendar.getInstance();
c.clear();
c.set(Calendar.YEAR, 1954);
c.set(Calendar.MONTH, Calendar.JUNE);
c.set(Calendar.DAY_OF_MONTH, 7);
Deathdate deathdate = new Deathdate(c.getTime());
vcard.setDeathdate(deathdate);

//partial date (e.g. just the month and date)
deathdate = new Deathdate(PartialDate.date(null, 6, 7)); //June 7
vcard.setDeathdate(deathdate);

//plain text value
deathdate = new Deathdate("In the 1950s");
vcard.setDeathdate(deathdate);
```

```
VCard vcard = ...
Deathdate deathdate = vcard.getDeathdate();

Date date = deathdate.getDate();
if (date != null){
  //property value is a date
}

PartialDate partialDate = deathdate.getPartialDate();
if (partialDate != null){
  //property value is a partial date
  int year = partialDate.getYear();
  int month = partialDate.getMonth();
}

String text = deathdate.getText();
if (text != null){
  //property value is plain text
}
```

# DEATHPLACE #
Defines the location of the person's death.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/question.png](https://ez-vcard.googlecode.com/svn/wiki/images/question.png) **4.0**`*` [RFC 6474 p.3](http://tools.ietf.org/html/rfc6474#page-3) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

`*` Defined in separate specification.

**Java class:** [ezvcard.property.Deathplace](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Deathplace.html)

```
VCard vcard = new VCard();

//text
Deathplace deathplace = new Deathplace("Wilmslow, Cheshire, England");
vcard.setDeathplace(deathplace);

//geo coordinates
deathplace = new Deathplace(53.325, -2.239);
vcard.setDeathplace(deathplace);

//URI
deathplace = new Deathplace();
deathplace.setUri("http://en.wikipedia.org/wiki/Wilmslow");
vcard.setDeathplace(deathplace);
```

```
VCard vcard = ...
Deathplace deathplace = vcard.getDeathplace();

String text = deathplace.getText();
if (text != null){
  //property value is plain text
}

Double latitude = deathplace.getLatitude();
Double longitude = deathplace.getLongitude();
if (latitude != null){
  //property value is a set of geo coordinates
}

String uri = deathplace.getUri();
if (uri != null){
  //property value is a URI
}
```

# EMAIL #
Defines an email address associated with the person.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.15](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.15](http://tools.ietf.org/html/rfc2426#page-15) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.36](http://tools.ietf.org/html/rfc6350#page-36) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Email](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Email.html)

```
VCard vcard = new VCard();

vcard.addEmail("johndoe@hotmail.com", EmailType.HOME);
Email workEmail = vcard.addEmail("jdoe@company.com", EmailType.WORK);
workEmail.setPref(1); //the most preferred email
```

# EXPERTISE #
Defines a professional subject area that the person has knowledge of. For example, if the person is a software engineer, he or she might list technologies such as "Java", "Web services", and "Agile development practices".

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/question.png](https://ez-vcard.googlecode.com/svn/wiki/images/question.png) **4.0**`*` [RFC 6715 p.3](http://tools.ietf.org/html/rfc6715#page-3) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

`*` Defined in separate specification.

**Java class:** [ezvcard.property.Expertise](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Expertise.html)

```
VCard vcard = new VCard();

Expertise expertise = new Expertise("Java programming");
expertise.setLevel(ExpertiseLevel.EXPERT);
vcard.addExpertise(expertise);
```

# FBURL #
Defines a URL that shows when the person is free/busy on their calendar.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.49](http://tools.ietf.org/html/rfc6350#page-49) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.FreeBusyUrl](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/FreeBusyUrl.html)

```
VCard vcard = new VCard();
vcard.addFbUrl("http://www.example.com/freebusy/janedoe");
```

# FN #
Defines the person's full name in a human-readable format.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.9](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.8](http://tools.ietf.org/html/rfc2426#page-8) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.28](http://tools.ietf.org/html/rfc6350#page-28) |
|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.FormattedName](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/FormattedName.html)

```
VCard vcard = new VCard();
vcard.setFormattedName("Dr. Gregory House M.D.");
```

# GENDER #
Defines the person's sex.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.32](http://tools.ietf.org/html/rfc6350#page-32) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Gender](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Gender.html)

```
VCard vcard = new VCard();
Gender gender = Gender.male();
vcard.setGender(gender);
```

```
VCard vcard = ...
Gender gender = vcard.getGender();
if (gender.isMale()){
  //gender is male
} else if (gender.isFemale()){
  //gender is female
}
//etc
```

# GEO #
A set of latitude/longitude coordinates. There is no rule for what these coordinates must represent, but the meaning could vary depending on the value of the vCard KIND property.

"individual": the location of the person's home or workplace.
"group": the location of the group's meeting place.
"org": the coordinates of the organization's headquarters.
"location": the coordinates of the location itself.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.16](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.16](http://tools.ietf.org/html/rfc2426#page-16) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.38](http://tools.ietf.org/html/rfc6350#page-38) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Geo](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Geo.html)

```
VCard vcard = new VCard();
vcard.setGeo(40.7127, -74.0059);
```

# HOBBY #
Defines a recreational activity that the person actively engages in. For example, if a person has a hobby of "hockey", it would mean that he likes to play hockey. Someone who just likes to <i>watch</i> hockey would list hockey as an INTEREST instead.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/question.png](https://ez-vcard.googlecode.com/svn/wiki/images/question.png) **4.0**`*` [RFC 6715 p.4](http://tools.ietf.org/html/rfc6715#page-4) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

`*` Defined in separate specification.

**Java class:** [ezvcard.property.Hobby](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Hobby.html)

```
VCard vcard = new VCard();

Hobby hobby = new Hobby("hockey");
hobby.setLevel(HobbyLevel.LOW);
vcard.addHobby(hobby);
```

# IMPP #
An instant message handle.  The handle is represented as a URI in the format "`<IM-PROTOCOL>:<IM-HANDLE>`".  For example, someone with a Yahoo! Messenger handle of "johndoe@yahoo.com" would have an IMPP vCard property value of "ymsgr:johndoe@yahoo.com".

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/question.png](https://ez-vcard.googlecode.com/svn/wiki/images/question.png) **3.0**`*` [RFC 4770](http://tools.ietf.org/html/rfc4770) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.36](http://tools.ietf.org/html/rfc6350#page-36) |
|:------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

`*` Defined in separate specification.

**Java class:** [ezvcard.property.Impp](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Impp.html)

```
VCard vcard = new VCard();

//URI
Impp impp = new Impp("aim:johndoe@aol.com");
vcard.addImpp(impp);

//static factory methods
impp = Impp.msn("janedoe@msn.com");
vcard.addImpp(impp);
```

# INTEREST #
Defines a recreational activity that the person is interested in. For example, if a person has an interest in "hockey", it would mean that he likes to watch hockey games. Someone who likes to actually <i>play</i> hockey would list hockey as a HOBBY instead.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/question.png](https://ez-vcard.googlecode.com/svn/wiki/images/question.png) **4.0**`*` [RFC 6715 p.5](http://tools.ietf.org/html/rfc6715#page-5) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

`*` Defined in separate specification.

**Java class:** [ezvcard.property.Interest](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Interest.html)

```
VCard vcard = new VCard();

Interest interest = new Interest("hockey");
interest.setLevel(InterestLevel.HIGH);
vcard.addInterest(interest);
```

# KEY #
Defines a public encryption key.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.22](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.26](http://tools.ietf.org/html/rfc2426#page-26) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.48](http://tools.ietf.org/html/rfc6350#page-48) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Key](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Key.html)

```
VCard vcard = new VCard();

//URL
Key key = new Key("http://www.mywebsite.com/my-public-key.pgp", KeyType.PGP);
vcard.addKey(key);

//binary data
byte data[] = ...
key = new Key(data, KeyType.PGP);
vcard.addKey(key);

//plain text value
key = new Key();
key.setText("...", KeyType.PGP);
vcard.addKey(key);
```

```
VCard vcard = ...
for (Key key : vcard.getKeys()){
  KeyType contentType = key.getContentType(); //e.g. "application/pgp-keys"

  String url = key.getUrl();
  if (url != null){
    //property value is a URL
    continue;
  }
  
  byte[] data = key.getData();
  if (data != null){
    //property value is binary data
    continue;
  }
  
  String text = key.getText();
  if (text != null){
    //property value is plain-text
    continue;
  }
}
```

# KIND #
Defines the type of entity that this vCard represents, such as an individual or an organization.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.25](http://tools.ietf.org/html/rfc6350#page-25) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Kind](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Kind.html)

```
VCard vcard = new VCard();

Kind kind = Kind.individual();
vcard.setKind(kind);
```

```
VCard vcard = ...
Kind kind = vcard.getKind();
if (kind.isIndividual()){
  //vCard contains information on an individual person
} else if (kind.isGroup()){
  //vCard contains information on a group of people
}
//etc
```

# LABEL #
Defines the exact text to put on the mailing label when sending snail mail to the person. Note that instances of this class should NEVER be added to a vCard! Instead, use the `Address.setLabel()` method to assign a mailing label to an ADR property.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.12](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.13](http://tools.ietf.org/html/rfc2426#page-13) | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **4.0**`*` |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Label](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Label.html)

`*`The label property is not supported in vCard version 4.0. Instead, labels are included as <i>parameters</i> to their corresponding ADR properties. When marshalling a vCard, ez-vcard will use either the label property or the LABEL parameter, depending on the requested vCard version.

**Orphaned labels**

ez-vcard defines an "orphaned label" as a label property that could not be assigned to an address (a label is assigned to an address if its list of TYPE parameters is identical to the address's list of TYPE parameters). The `VCard.addOrphanedLabel()` method can be used to add such labels to a vCard, but its use is strongly discouraged. The `VCard.getOrphanedLabels()` method can be useful when parsing version 2.1 or 3.0 vCards in order to retrieve any label properties that the parser could not assign to an address.

# LANG #
Defines a language that the person speaks.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.37](http://tools.ietf.org/html/rfc6350#page-37) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Language](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Language.html)

```
VCard vcard = new VCard();

Language lang = vcard.addLanguage("en");
lang.setPref(1); //most preferred

lang = vcard.addLanguage("fr");
lang.setPref(2); //second-most preferred
```

# LOGO #
Defines a company logo.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.17](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.18](http://tools.ietf.org/html/rfc2426#page-18) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.40](http://tools.ietf.org/html/rfc6350#page-40) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Logo](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Logo.html)

```
VCard vcard = new VCard();

//URL
Logo logo = new Logo("http://www.ourcompany.com/our-logo.png", ImageType.PNG);
vcard.addLogo(logo);

//binary data
byte data[] = ...
logo = new Logo(data, ImageType.PNG);
vcard.addLogo(logo);
```

```
VCard vcard = ...
for (Logo logo : vcard.getLogos()){
  ImageType contentType = logo.getContentType(); //e.g. "image/png"

  String url = logo.getUrl();
  if (url != null){
    //property value is a URL
    continue;
  }
  
  byte[] data = logo.getData();
  if (data != null){
    //property value is binary data
    continue;
  }
}
```

# MAILER #
Defines the email client that the person uses.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.15](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.15](http://tools.ietf.org/html/rfc2426#page-15) | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **4.0** |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Mailer](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Mailer.html)

```
VCard vcard = new VCard();
vcard.setMailer("Thunderbird");
```

# MEMBER #
Defines the members that make up the group. This property can only be used if the vCard's KIND property is set to "group".

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.41](http://tools.ietf.org/html/rfc6350#page-41) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Member](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Member.html)

```
VCard vcard = new VCard();

//kind property must be set to "group" in order to add members
vcard.setKind(Kind.group());

//static factory methods
Member member = Member.email("johndoe@hotmail.com");
vcard.addMember(member);

//reference another vCard by putting its UID property here
member = new Member("urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af");
vcard.addMember(member);
```

# N #
Defines the individual components of the person's name.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.9](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.9](http://tools.ietf.org/html/rfc2426#page-9) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.29](http://tools.ietf.org/html/rfc6350#page-29) |
|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.StructuredName](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/StructuredName.html)

```
VCard vcard = new VCard();

StructuredName n = new StructuredName();
n.setFamily("House");
n.setGiven("Gregory");
n.addPrefix("Dr");
n.addSuffix("MD");
vcard.setStructuredName(n);
```

# NAME #
Defines a textual representation of the SOURCE property.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.5](http://tools.ietf.org/html/rfc2426#page-5) | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **4.0** |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.SourceDisplayText](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/SourceDisplayText.html)

```
VCard vcard = new VCard();
vcard.setSourceDisplayText("My vCard");
```

# NICKNAME #
Defines the person's nicknames.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.9](http://tools.ietf.org/html/rfc2426#page-9) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.29](http://tools.ietf.org/html/rfc6350#page-29) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Nickname](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Nickname.html)

```
VCard vcard = new VCard();
vcard.setNickname("Rick", "Ricky", "Bobby");
```

# NOTE #
Defines a free-form text field that contains miscellaneous information.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.19](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.21](http://tools.ietf.org/html/rfc2426#page-21) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.44](http://tools.ietf.org/html/rfc6350#page-44) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Note](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Note.html)

```
VCard vcard = new VCard();
Note note = vcard.addNote("This is a\nmiscellaneous comment."); //can contain newlines
note.setLanguage("en-us");
```

# ORG #
Defines a list of organizations the person belongs to. The list is ordered. It begins with the broadest organization and ends with the most specific.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.19](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.20](http://tools.ietf.org/html/rfc2426#page-20) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.40](http://tools.ietf.org/html/rfc6350#page-40) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Organization](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Organization.html)

```
VCard vcard = new VCard();
vcard.setOrganization("Google", "GMail Team", "Spam Detection Team");
```

# ORG-DIRECTORY #
Defines a URI that can be used to retrieve information about the person's co-workers.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/question.png](https://ez-vcard.googlecode.com/svn/wiki/images/question.png) **4.0**`*` [RFC 6715 p.6](http://tools.ietf.org/html/rfc6715#page-6) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

`*` Defined in separate specification.

**Java class:** [ezvcard.property.OrgDirectory](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/OrgDirectory.html)

```
VCard vcard = new VCard();
vcard.addOrgDirectory("http://www.company.com/staff");
```

# PHOTO #
Defines a photo, such as the person's portrait.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.10](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.10](http://tools.ietf.org/html/rfc2426#page-10) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.30](http://tools.ietf.org/html/rfc6350#page-30) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Photo](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Photo.html)

```
VCard vcard = new VCard();

//URL
Photo photo = new Photo("http://www.mywebsite.com/my-photo.jpg", ImageType.JPEG);
vcard.addPhoto(photo);

//binary data
byte data[] = ...
photo = new Photo(data, ImageType.JPEG);
vcard.addPhoto(photo);
```

```
VCard vcard = ...
for (Photo photo : vcard.getPhotos()){
  PhotoType contentType = photo.getContentType(); //e.g. "image/jpeg"

  String url = photo.getUrl();
  if (url != null){
    //property value is a URL
    continue;
  }
  
  byte[] data = photo.getData();
  if (data != null){
    //property value is binary data
    continue;
  }
}
```

# PRODID #
Identifies the software application that created the vCard.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.21](http://tools.ietf.org/html/rfc2426#page-21) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.44](http://tools.ietf.org/html/rfc6350#page-44) |
|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.ProductId](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/ProductId.html)

```
VCard vcard = new VCard();
vcard.setProdId("ez-vcard 0.6.0");
```

# PROFILE #
Simply identifies the vCard as a "vCard".

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.5](http://tools.ietf.org/html/rfc2426#page-5) | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **4.0** |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Profile](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Profile.html)

```
VCard vcard = new VCard();
Profile profile = new Profile();
vcard.setProfile(profile);
```

# RELATED #
Defines someone that the person is related to.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.42](http://tools.ietf.org/html/rfc6350#page-42) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Related](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Related.html)

```
VCard vcard = new VCard();

//static factory methods
Related related = Related.email("bob.smith@example.com");
related.addType(RelatedType.CO_WORKER);
related.addType(RelatedType.FRIEND);
vcard.addRelated(related);

//free-form text
related = new Related();
related.setText("Edna Smith");
related.addType(RelatedType.SPOUSE);
vcard.addRelated(related);

//reference another vCard by putting its UID property here
related = new Related("urn:uuid:03a0e51f-d1aa-4385-8a53-e29025acd8af");
related.addType(RelatedType.SIBLING);
vcard.addRelated(related);
```

# REV #
Defines the date that the vCard was last modified by its owner.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.19](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.22](http://tools.ietf.org/html/rfc2426#page-22) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.45](http://tools.ietf.org/html/rfc6350#page-45) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Revision](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Revision.html)

```
VCard vcard = new VCard();
vcard.setRevision(new Date());
```

# ROLE #
Defines the function that the person plays within his or her organization.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.17](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.18](http://tools.ietf.org/html/rfc2426#page-18) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.39](http://tools.ietf.org/html/rfc6350#page-39) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Role](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Role.html)

```
VCard vcard = new VCard();
vcard.addRole("Project Leader");
```

# SORT-STRING #
Defines a string that should be used when an application sorts this vCard in some way.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.22](http://tools.ietf.org/html/rfc2426#page-22) | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **4.0**`*` |
|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.SortString](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/SortString.html)

`*`The SORT-STRING property is not supported in 4.0.  Instead, a SORT-AS parameter can be added to the N (`StructuredName`) and/or ORG (`Organization`) types.

```
//3.0
VCard vcard = new VCard();

StructuredName n = new StructuredName();
n.setFamily("d'Armour");
n.setGiven("Miles");
vcard.setStructuredName(n);
vcard.setSortString("Armour");
```

```
//4.0
VCard vcard = new VCard();

StructuredName n = new StructuredName();
n.setFamily("d'Armour");
n.setGiven("Miles");
n.setSortAs("Armour");
vcard.setStructuredName(n);
```

# SOUND #
Defines a sound, such as the correct pronunciation of the person's name.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.20](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.23](http://tools.ietf.org/html/rfc2426#page-23) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.45](http://tools.ietf.org/html/rfc6350#page-45) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Sound](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Sound.html)

```
VCard vcard = new VCard();

//URL
Sound sound = new Sound("http://www.mywebsite.com/my-name.ogg", SoundType.OGG);
vcard.addSound(sound);

//binary data
byte data[] = ...
sound = new Sound(data, SoundType.OGG);
vcard.addSound(sound);
```

```
VCard vcard = ...
for (Sound sound : vcard.getSounds()){
  SoundType contentType = sound.getContentType(); //e.g. "audio/ogg"

  String url = sound.getUrl();
  if (url != null){
    //property value is a URL
    continue;
  }
  
  byte[] data = sound.getData();
  if (data != null){
    //property value is binary data
    continue;
  }
}
```

# SOURCE #
Defines a URL that can be used to retrieve the most up-to-date version of the vCard.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.5](http://tools.ietf.org/html/rfc2426#page-5) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.24](http://tools.ietf.org/html/rfc6350#page-24) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Source](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Source.html)

```
VCard vcard = new VCard();
vcard.addSource("http://www.company.com/employees/doe_john.vcf");
```

# TEL #
Defines a telephone number.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.13](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.14](http://tools.ietf.org/html/rfc2426#page-14) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.34](http://tools.ietf.org/html/rfc6350#page-34) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Telephone](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Telephone.html)

```
VCard vcard = new VCard();

//text
Telephone tel = new Telephone("(123) 555-6789");
tel.addType(TelephoneType.HOME);
tel.setPref(2); //the second-most preferred
vcard.addTelephoneNumber(tel);

//URI (vCard version 4.0 only)
TelUri uri = new TelUri.Builder("+1-800-555-9876").extension("111").build();
tel = new Telephone(uri);
tel.addType(TelephoneType.WORK);
tel.setPref(1); //the most preferred
vcard.addTelephoneNumber(tel);
```

# TZ #
Defines the timezone that the person lives/works in.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.16](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.16](http://tools.ietf.org/html/rfc2426#page-16) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.22](http://tools.ietf.org/html/rfc6350#page-22) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Timezone](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Timezone.html)

```
VCard vcard = new VCard();

Timezone tz = new Timezone(-5, 0, "America/New_York");
vcard.addTimezone(tz);

//using a Java "TimeZone" object
java.util.TimeZone javaTz = java.util.TimeZone.getTimeZone("America/New_York");
tz = new Timezone(javaTz);
vcard.addTimezone(tz);
```

# TITLE #
Defines the person's title in his or her organization.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.17](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.17](http://tools.ietf.org/html/rfc2426#page-17) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.39](http://tools.ietf.org/html/rfc6350#page-39) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Title](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Title.html)

```
VCard vcard = new VCard();
vcard.addTitle("Research Scientist");
```

# UID #
Defines a globally unique identifier for this vCard.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.21](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.24](http://tools.ietf.org/html/rfc2426#page-24) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.46](http://tools.ietf.org/html/rfc6350#page-46) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Uid](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Uid.html)

```
VCard vcard = new VCard();
vcard.setUid("urn:uuid:b8767877-b4a1-4c70-9acc-505d3819e519");

//generate a random UID
Uid uid = Uid.random();
vcard.setUid(uid);
```

# URL #
Defines a URL that points to the person's homepage or business website.

| ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **2.1** [vCard 2.1 p.21](http://www.imc.org/pdi/vcard-21.doc) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **3.0** [RFC 2426 p.25](http://tools.ietf.org/html/rfc2426#page-25) | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.47](http://tools.ietf.org/html/rfc6350#page-47) |
|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Url](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Url.html)

```
VCard vcard = new VCard();
vcard.addUrl("http://www.company.com");
```

# XML #
Contains an XML element that was not recognized when parsing an xCard (XML-formatted vCard).

| ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **2.1** | ![https://ez-vcard.googlecode.com/svn/wiki/images/x.png](https://ez-vcard.googlecode.com/svn/wiki/images/x.png) **3.0** | ![https://ez-vcard.googlecode.com/svn/wiki/images/check.png](https://ez-vcard.googlecode.com/svn/wiki/images/check.png) **4.0** [RFC 6350 p.27](http://tools.ietf.org/html/rfc6350#page-27) |
|:------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

**Java class:** [ezvcard.property.Xml](https://ez-vcard.googlecode.com/svn/javadocs/latest/index.html?ezvcard/property/Xml.html)

```
VCard vcard = new VCard();
Xml xml = new Xml("<b>Some xml</b>");
vcard.addXml(xml);
```