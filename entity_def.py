################################################################################
# Program Name: entity_def.py
# Author: Mathew Kagel
# Date: 2018-06-10
# Description: This program has all class definitions for this API. 
################################################################################
from google.appengine.ext import ndb

class User(ndb.Model):
	id = ndb.StringProperty()
	uid = ndb.StringProperty()
	# first_name, last_name, zip_code came from
	# @175 from Piazza: Final Project Cloud-Only: Does a 'User' or 'UserProfile' entity count?
	# I needed four attributes for users also, so I took the three above and
	# threw in an occupation that is relevant to the properties the user
	# has listed in this application.
	first_name = ndb.StringProperty()
	last_name = ndb.StringProperty()
	zip_code = ndb.IntegerProperty()
	occupation = ndb.StringProperty()

class Property(ndb.Model):
	id = ndb.StringProperty()
	owner = ndb.StringProperty(required=True)
	type = ndb.StringProperty(required=True)
	value = ndb.FloatProperty()
	acreage = ndb.FloatProperty()
	location = ndb.StringProperty()

