finagle-gntp
============

Growl Notification Transport Protocol (GNTP) implementation for Finagle.

## Usage

### Register

<pre><code>val client = GntpClient("localhost")
client(Register(Application("TestApp"), List(NotificaionType("Test1"))))
</code></pre>

### Notify

<pre><code>client(Notify(applicationName="TestApp", name="Test1", title="Title", text="Hello!"))
</code></pre>
