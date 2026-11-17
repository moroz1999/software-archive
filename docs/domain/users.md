# Users

Visitors read the archive freely; changing it takes an account.

## Account
An account has a user name, an email and a password, nothing else. The user name and the email each belong to one account only, whatever their letter case. A user signs in with either of them and the password.

## Registration and confirmation
A new account is unconfirmed: it cannot sign in. Registration sends a confirmation link to the email; the link works for one week and only once. Opening it confirms the account and signs the user in.
An unconfirmed user can ask for a new link by giving their user name or email and password; the new link replaces the old one.
An account left unconfirmed after its link expired does not hold its user name or email: registering with them again replaces it.

## Sessions
A signed-in user stays signed in until they sign out. "Remember me" keeps them signed in across browser restarts for 30 days of inactivity; without it the session ends with the browser.

## User groups
What a user may do comes from their groups, and for the entries of the archive also from who added the entry.

| Group | Who | May |
|---|---|---|
| Public visitor | Everyone not signed in | Read the archive |
| Member | Every confirmed account | Add software, releases, people and groups; edit and delete what they added |
| Moderator | Given on purpose | Delete comments |
| Administrator | Given on purpose | Everything |

A member adds releases to any software. A release can be edited and deleted by whoever added it and by whoever added its software. Categories are kept by administrators only. Every entry is added by a signed-in user, who is remembered as its adder. An entry whose adder's account was deleted keeps no adder: only administrators change it. A label is edited, deleted and made into a new person or group by whoever added it and by administrators; one taken away from an owner, only when they may change that owner too. Only administrators say whose a label is: they attach labels to existing owners and take them away.

The first confirmed account of the archive becomes an administrator.

A new archive gets its first administrator from the server it runs on, without a letter: whoever runs the archive gives the user name, email and password, and the account is confirmed at once. This works only while the archive has no administrator; the user name and email obey the same rules as in registration.

## Managing users
Administrators see every account of the archive — confirmed or still waiting for its link — with its email, the day it registered and its groups, and find one by a part of its user name or email.
An administrator edits any account, confirmed or not: its user name, its email and, for a confirmed user, the groups given on purpose — administrator and moderator. Being a member comes with confirmation and is not given or taken away; an unconfirmed account gets no groups.
A new user name and email obey the rules of registration: they must not belong to another account, whatever the letter case, and an abandoned account gives them up. A new email is taken at once, with no letter: the administrator answers for it. An account still waiting for its link keeps the link, which goes on working.
The password is not edited by an administrator.
The archive keeps at least one administrator: the last one cannot lose the group, not even by their own hand.
A user's new groups take effect once their session renews itself, within a quarter of an hour, without signing them out.
