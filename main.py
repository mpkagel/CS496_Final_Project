################################################################################
# Program Name: main.py
# Author: Mathew Kagel
# Date: 2018-06-10
# Description: This program has handlers for Users and Properties. There is also
#   a handler for the root directory. This is the back end for a REST API that
#   allows users to login and look at their properties on a front end. The project
#   has an Android based front end in mind.
################################################################################
# The code below was largely adapted from the code at:
# https://github.com/GoogleCloudPlatform/python-docs-samples/tree/master/appengine/standard/hello_world
import webapp2
from google.appengine.ext import ndb
import json
import sys
import time
from google.appengine.api import urlfetch
from google.appengine.ext.webapp import template
import urllib
from entity_def import User
from entity_def import Property 

firebase_api_key = "AIzaSyAugzEPtecaXrStpKQbkEfe9Hf1AEGDHOI"
logged_in = False
logged_in_as = ""
refresh_token = ""

class PropertyHandler(webapp2.RequestHandler):
	def post(self, id=None):
		global logged_in
		if logged_in:
			prop_data = json.loads(self.request.body)
			prop_owner = None
			prop_type = "None"
			prop_value = 0
			prop_acreage = 0
			prop_location = "None"
			# If the user has entered values, the "None"s and
			# 0's will be overwritten. Otherwise, they get used.
			# This prevents empty values from getting set for
			# attributes.
			if prop_data['owner']:
				prop_owner = prop_data['owner']
			if prop_data['type']:
				prop_type = prop_data['type']
			if prop_data['value']:
				prop_value = float(prop_data['value'])
			if prop_data['acreage']:
				prop_acreage = float(prop_data['acreage'])
			if prop_data['location']:
				prop_location = prop_data['location']

			new_prop = Property(
				owner=prop_owner,
				type=prop_type,
				value=prop_value,
				acreage=prop_acreage,
				location=prop_location
			)
			new_prop.put() # Add to datastore
			new_prop.id = new_prop.key.urlsafe() # Get key from newly generated entity
			new_prop.put() # Store key in id attribute
			prop_dict = new_prop.to_dict()
			prop_dict['self'] = '/property/' + new_prop.id
			# The reason for time.sleep() sort of comes from:
			# https://cloud.google.com/appengine/articles/life_of_write
			# The function returns successfully after the change has been committed,
			# but the apply happens asynchronously, and I was unable to figure out
			# how to catch a signal from the datastore apply, so time.sleep() 
			# just waits it out.
			# The sleep time on add property is greater than for others in this program,
			# because testing showed that it needed to be. I don't know why.
			time.sleep(0.2) # Giving the datastore time to apply the change
			self.response.write(json.dumps(prop_dict)) # Send new entity back to front end
		else:
			self.response.status = 403 # If not logged in, send 403

	def delete(self, id=None):
		global logged_in
		if logged_in:
			if id:
				p_qry = Property.query(Property.id == id)
				p = p_qry.fetch()
				if len(p) != 1:
					self.response.status = 404 # If the property can't be found, send 404
				else:
					p_d = p[0].to_dict()
					p_d['self'] = '/property/' + id
					p[0].key.delete() # Delete entity from datastore
					time.sleep(0.125) # Giving the datastore time to apply the change
					self.response.write("Delete" + json.dumps(p_d)) # Send delete confirmation to front end
		else:
			self.response.status = 403 # If not logged in, send 403

	def patch(self, id=None):
		global logged_in
		if logged_in:
			if id:
				p_qry = Property.query(Property.id == id)
				p = p_qry.fetch()
				if len(p) != 1:
					self.response.status = 404 # If the property can't be found, send 404
				else:
					edit_prop_data = json.loads(self.request.body)
					prop_data = p[0]
					prop_type = "None"
					prop_value = 0
					prop_acreage = 0
					prop_location = "None"
					# If the user has entered values, the "None"s and
					# 0's will be overwritten. Otherwise, they get used.
					# This prevents empty values from getting set for
					# attributes.
					if edit_prop_data['type']:
						prop_type = edit_prop_data['type']
					if edit_prop_data['value']:
						prop_value = float(edit_prop_data['value'])
					if edit_prop_data['acreage']:
						prop_acreage = float(edit_prop_data['acreage'])
					if edit_prop_data['location']:
						prop_location = edit_prop_data['location']
					prop_data.type = prop_type
					prop_data.value = prop_value
					prop_data.acreage = prop_acreage
					prop_data.location = prop_location
					prop_data.put() # Update entity in datastore
					upd_prop_data = prop_data.to_dict()
					upd_prop_data['self'] = '/property/' + id
					time.sleep(0.125) # Giving the datastore time to apply the change
					self.response.write(json.dumps(upd_prop_data)) # Send updated property to front end
		else:
			self.response.status = 403 # If not logged in, send 403

	def get(self, id=None):
		global logged_in
		if logged_in:
			if id:
				p_qry = Property.query(Property.id == id)
				p = p_qry.fetch()
				if len(p) != 1:
					self.response.status = 404 # If the property can't be found, send 404
				else:
					p_d = p[0].to_dict()
					p_d['self'] = '/property/' + id
					self.response.write(json.dumps(p_d))
		else:
			self.response.status = 403 # If not logged in, send 403

class PropertiesHandler(webapp2.RequestHandler):
	# This looks up properties specific to a certain user.
	# So this queries a Property where the owner attribute of
	# the Property is equal to the user id passed into the
	# handler.
	# The id used for Property ownership is the datastore id
	# for the user, not their Firebase id.
	def get(self, id=None):
		if id:
			global logged_in
			global logged_in_as
			if logged_in:
				p_qry = Property.query(Property.owner == id)
				props_data = p_qry.fetch()
				if (len(props_data) == 0):
					self.response.status = 200 # If there are no properties, this is still valid
				else:
					ps_d = []
					for p in props_data:
						p_d = p.to_dict()
						p_d['self'] = '/property/' + p.id
						ps_d.append(p_d)
					self.response.write(json.dumps(ps_d))
			else:
				self.response.status = 403 # If not logged in, send 403
		else:
			self.response.status = 404 # If there is no user id, send 404

class UserHandler(webapp2.RequestHandler):
	def post(self, id=None):
		global firebase_api_key
		global logged_in
		global logged_in_as
		new_creds = json.loads(self.request.body)

		form_fields = {
			'email': new_creds['email'],
			'password': new_creds['password'],
			'returnSecureToken': True
		}
		json_form = json.dumps(form_fields)

		try:
			headers = {'Content-Type': 'application/json'}
			result = urlfetch.fetch( # Firebase endpoint for new user
				url="https://www.googleapis.com/identitytoolkit/v3/relyingparty/signupNewUser?key=" + firebase_api_key,
				payload=json_form,
				method=urlfetch.POST,
				headers=headers
			)
			if result.status_code >= 400: # Pass Firebase error on to front end
				error = json.loads(result.content)
				self.response.status = result.status_code
				self.response.write(json.dumps(error['error']['message']))
			else:
				firebase_creds = json.loads(result.content)
				user_first_name = "None"
				user_last_name = "None"
				user_zip_code = 0
				user_occupation = "None"
				# If the user has entered values, the "None"s and
				# 0's will be overwritten. Otherwise, they get used.
				# This prevents empty values from getting set for
				# attributes.
				if new_creds['first']:
					user_first_name = new_creds['first']
				if new_creds['last']:
					user_last_name = new_creds['last']
				if new_creds['zip']:
					user_zip_code = int(new_creds['zip'])
				if new_creds['occupation']:
					user_occupation = new_creds['occupation']
				new_user = User(uid=firebase_creds['localId'],
								first_name=user_first_name,
								last_name=user_last_name,
								zip_code=user_zip_code,
								occupation=user_occupation
								)
				new_user.put() # Add to datastore
				new_user.id = new_user.key.urlsafe() # Get key and assign to user id
				# Users in this back end have two ids, an id from the Google Cloud datastore,
				# and an id from the Google Firebase.
				# This app authenticates users by their Firebase id, but all of
				# the handlers identify the user by their datastore id.
				# The datastore id is what is used in the owner attribute of Property as well.
				new_user.put() # Update datastore
				user_dict = new_user.to_dict()
				user_dict['self'] = '/user/' + new_user.key.urlsafe()
				self.response.write(json.dumps(user_dict)) # Send new user info back to front end

		except urlfetch.Error:
			logging.exception("Caught exception fetching url new user sign up")
		
	def delete(self, id=None):
		global firebase_api_key
		global logged_in
		global logged_in_as
		global refresh_token
		if logged_in:
			if id:
				u_qry = User.query(User.id == id)
				u = u_qry.fetch()
				if len(u) != 1:
					self.response.status = 404 # If the user can't be found, send 404
				else:
					form_fields = {
						'grant_type': "refresh_token",
						'refresh_token': refresh_token
					}

					try:
						form_data = urllib.urlencode(form_fields)
						headers = {'Content-Type': 'application/x-www-form-urlencoded'}
						result = urlfetch.fetch( # Firebase endpoint to trade refresh token for id token
							url="https://securetoken.googleapis.com/v1/token?key=" + firebase_api_key,
							payload=form_data,
							method=urlfetch.POST,
							headers=headers
						)
						if result.status_code >= 400: # Pass Firebase error on to front end
							error = json.loads(result.content)
							self.response.status = result.status_code
							self.response.write(json.dumps(error['error']['message']))
						else:
							firebase_creds = json.loads(result.content)
							id_token = firebase_creds['id_token']
							firebase_creds = json.loads(result.content)

							form_fields = {
								'idToken': id_token,
							}
							json_form = json.dumps(form_fields)

							try:
								headers = {'Content-Type': 'application/json'}
								result = urlfetch.fetch( # Firebase endpoint to delete user
									url="https://www.googleapis.com/identitytoolkit/v3/relyingparty/deleteAccount?key=" + firebase_api_key,
									payload=json_form,
									method=urlfetch.POST,
									headers=headers
								)
								if result.status_code >= 400: # Pass Firebase error on to front end
									error = json.loads(result.content)
									self.response.status = result.status_code
									self.response.write(json.dumps(error['error']['message']))
								else:
									u_d = u[0].to_dict()
									u_d['self'] = '/user/' + id
									u[0].key.delete() # Delete user from datastore
									p_qry = Property.query(Property.owner == id)
									props_data = p_qry.fetch()
									if (len(props_data) > 0): # Delete user's Property from datastore as well
										for i in range(0, len(props_data)):
											props_data[i].key.delete()
									time.sleep(0.125) # Giving the datastore time to apply the change
									self.response.write("Delete" + json.dumps(u_d))
							except urlfetch.Error:
								logging.exception("Caught exception fetching url new user sign up")
					except urlfetch.Error:
						logging.exception("Caught exception fetching url new user sign up")
		else:
			self.response.status = 403 # If not logged in, send 403

	def patch(self, id=None):
		global logged_in
		if logged_in:
			if id:
				u_qry = User.query(User.id == id)
				u = u_qry.fetch()
				if len(u) != 1:
					self.response.status = 404 # If the user can't be found, send 404
				else:
					edit_user_data = json.loads(self.request.body)
					user_data = u[0]
					user_data.first_name = "None"
					user_data.last_name = "None"
					user_data.zip_code = 0
					user_data.occupation = "None"
					# If the user has entered values, the "None"s and
					# 0's will be overwritten. Otherwise, they get used.
					# This prevents empty values from getting set for
					# attributes.
					if edit_user_data['first']:
						user_data.first_name = edit_user_data['first']
					if edit_user_data['last']:
						user_data.last_name = edit_user_data['last']
					if edit_user_data['zip']:
						user_data.zip_code = int(edit_user_data['zip'])
					if edit_user_data['occupation']:
						user_data.occupation = edit_user_data['occupation']
					user_data.put() # Update datastore
					upd_user_data = user_data.to_dict()
					upd_user_data['self'] = '/user/' + id
					self.response.write(json.dumps(upd_user_data)) # Send updated user to front end
		else:
			self.response.status = 403 # If not logged in, send 403

	def get(self, id=None):
		global logged_in
		global logged_in_as
		if id:
			if logged_in:
				u_qry = User.query(User.id == id)
				u = u_qry.fetch()
				if len(u) != 1:
					self.response.status = 404 # If the user can't be found, send 404
				else:
					u_d = u[0].to_dict()
					u_d['self'] = '/user/' + u[0].key.urlsafe()
					self.response.status = 200
					self.response.write(json.dumps(u_d))
			else:
				self.response.status = 403 # If not logged in, send 403
		else:
			# If there's no id, but there is a user logged in, it is assumed
			# that they have been authenticated, so their user id is sent to
			# the front end.
			if logged_in:
				user_entity_id = {'id': logged_in_as}
				self.response.status = 200
				self.response.write(json.dumps(user_entity_id))
			else:
				self.response.status = 403 # If not logged in, send 403

class UserSigninHandler(webapp2.RequestHandler):
	def post(self):
		global firebase_api_key
		global logged_in
		global logged_in_as
		global refresh_token
		creds = json.loads(self.request.body)

		form_fields = {
			'email': creds['email'],
			'password': creds['password'],
			'returnSecureToken': True
		}
		json_form = json.dumps(form_fields)

		try:
			headers = {'Content-Type': 'application/json'}
			result = urlfetch.fetch( # Firebase endpoint to verify user credentials for pre-existing user
				url="https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=" + firebase_api_key,
				payload=json_form,
				method=urlfetch.POST,
				headers=headers
			)
			if result.status_code >= 400: # Pass Firebase error on to front end
				error = json.loads(result.content)
				self.response.status = result.status_code
				self.response.write(json.dumps(error['error']['message']))
			else:
				firebase_creds = json.loads(result.content)
				refresh_token = firebase_creds['refreshToken']
				u_qry = User.query(User.uid == firebase_creds['localId'])
				u = u_qry.fetch()
				u_d = u[0].to_dict()
				u_d['self'] = '/user/' + u[0].id
				logged_in = True # Log user in
				logged_in_as = u[0].id # Note which user is logged in
				self.response.write(json.dumps(u_d)) # Send response to front end

		except urlfetch.Error:
			logging.exception("Caught exception fetching url user sign in")

    
class MainPage(webapp2.RequestHandler):
    def get(self):
    	global logged_in
    	global logged_in_as
    	global refresh_token
    	logged_in = False # The root url logs the user out
    	logged_in_as = ""
    	refresh_token = ""
    	
# Add Patch to HTTP request method list
allowed_methods = webapp2.WSGIApplication.allowed_methods
new_allowed_methods = allowed_methods.union(('PATCH',))
webapp2.WSGIApplication.allowed_methods = new_allowed_methods

app = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/user/(.*)/properties', PropertiesHandler),
    ('/user/login', UserSigninHandler),
    ('/user/(.*)', UserHandler),
    ('/property/(.*)', PropertyHandler),
], debug=True)



