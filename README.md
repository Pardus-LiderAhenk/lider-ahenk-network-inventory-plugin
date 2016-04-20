# lider-ahenk-network-inventory

Lider Ahenk network inventory plugin is used to scan network where Lider reside in and collect valuable information about connected devices. It also provides file distribution via [SCP](http://man7.org/linux/man-pages/man1/scp.1.html) and Ahenk installation.

## Building from Source

* Just run `mvn clean install`

## How to Install & Run?

* Start Lider (Karaf container).
* Add network-inventory-feature to Karaf via `feature:repo-add  mvn:tr.org.liderahenk/lider-network-inventory-feature/1.0.0/xml/features`
* Install & run bundles `feature:install lider-network-inventory`

## License

Lider Ahenk and its sub projects are licensed under the [LGPL v3](https://github.com/Pardus-Kurumsal/lider-ahenk-network-inventory/blob/master/LICENSE).
